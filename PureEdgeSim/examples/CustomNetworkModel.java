package examples;

import com.mechalikh.pureedgesim.Network.DefaultNetworkModel;
import com.mechalikh.pureedgesim.Network.FileTransferProgress;
import com.mechalikh.pureedgesim.Network.NetworkModel;
import com.mechalikh.pureedgesim.ScenarioManager.SimulationParameters;
import com.mechalikh.pureedgesim.ScenarioManager.SimulationParameters.TYPES;
import com.mechalikh.pureedgesim.SimulationManager.SimulationManager;
import com.mechalikh.pureedgesim.TasksGenerator.Task;

public class CustomNetworkModel extends DefaultNetworkModel {

	private static final int MAX_NUMBER_OF_REPLICAS = 8;

	public CustomNetworkModel(SimulationManager simulationManager) {
		super(simulationManager);
	}

	@Override
	protected void transferFinished(FileTransferProgress transfer) {
		if (transfer.getTransferType() == FileTransferProgress.Type.TASK && SimulationParameters.ENABLE_REGISTRY
				&& "CACHE".equals(SimulationParameters.registry_mode)) {
			// the offloading request has been received, now pull the container in order to
			// execute the task
			pullContainer(transfer.getTask());

			transfer.getTask().setReceptionTime(simulationManager.getSimulation().clock());
			updateEnergyConsumption(transfer, "Destination");

			// Update logger parametersa
			simulationManager.getSimulationLogger().updateNetworkUsage(transfer);

			// Delete the transfer from the queue
			transferProgressList.remove(transfer);

		} else if (transfer.getTransferType() == FileTransferProgress.Type.CONTAINER) {
			// the container has been downloaded, keep it in cache
			keepReplica(transfer.getTask());

			// Update logger parameters
			simulationManager.getSimulationLogger().updateNetworkUsage(transfer);

			transfer.getTask().setReceptionTime(simulationManager.getSimulation().clock());

			// execute the task
			containerDownloadFinished(transfer);

			updateEnergyConsumption(transfer, "Container");

			// Delete the transfer from the queue
			transferProgressList.remove(transfer);

		} else // use the default method to handle everything else
			super.transferFinished(transfer);
	}

	private void keepReplica(Task task) {
		// Check if there are enough replicas before keeping a new one
		CustomEdgeDevice edgeDevice = (CustomEdgeDevice) task.getEdgeDevice();
		if (canKeepReplica(edgeDevice, task)) {
			if (edgeDevice.getResources().getAvailableStorage() > task.getContainerSize()) {
				saveImage(edgeDevice, task);
			} else {
				// while the storage is not enough
				freeStorage(edgeDevice, task);
			}
			// if the memory is enough
			if (edgeDevice.getResources().getAvailableStorage() > task.getContainerSize()) {
				saveImage(edgeDevice, task);
			}
		}
	}

	private boolean canKeepReplica(CustomEdgeDevice edgeDevice, Task task) {
		return ("CACHE".equals(SimulationParameters.registry_mode) && ((CustomEdgeDevice) edgeDevice.getOrchestrator())
				.countContainer(task.getApplicationID()) < MAX_NUMBER_OF_REPLICAS);
	}

	private void freeStorage(CustomEdgeDevice edgeDevice, Task task) {
		// while the available storage is not enough
		double min = 0;
		while (storageIsEnough(edgeDevice, task)) {
			min = edgeDevice.getMinContainerCost();
			if (edgeDevice.getCost(task) < min || min == -1) {
				// delete the app with the highest cost
				edgeDevice.deleteMinAapp();
			} else {
				break;
			}
		}
	}

	private boolean storageIsEnough(CustomEdgeDevice edgeDevice, Task task) {
		return (edgeDevice.getResources().getAvailableStorage() < task.getContainerSize()
				&& edgeDevice.getResources().getStorageMemory() > task.getContainerSize());
	}

	private void saveImage(CustomEdgeDevice edgeDevice, Task task) {
		edgeDevice.getResources()
				.setAvailableMemory(edgeDevice.getResources().getAvailableStorage() - task.getContainerSize());
		edgeDevice.cache.add(task);
		double[] array = new double[2];
		array[0] = task.getApplicationID();
		array[1] = task.getEdgeDevice().getId();
		((CustomEdgeDevice) edgeDevice.getOrchestrator()).Remotecache.add(array);
	}

	private void pullContainer(Task task) {
		if (!((CustomEdgeDevice) task.getEdgeDevice().getOrchestrator()).hasRemoteContainer(task.getApplicationID())) {
			// No replica found
			scheduleNow(this, NetworkModel.DOWNLOAD_CONTAINER, task);
		} else { // replica found
			pullFromCache(task);
		}
	}

	private void pullFromCache(Task task) {

		if (((CustomEdgeDevice) task.getVm().getHost().getDatacenter()).hasContainer(task.getApplicationID())
				|| ((CustomEdgeDevice) task.getVm().getHost().getDatacenter()).getType() == TYPES.CLOUD) {
			// This device has a replica in its cache, so execute a task directly
			scheduleNow(simulationManager, SimulationManager.EXECUTE_TASK, task);
		} else {
			double from = ((CustomEdgeDevice) task.getEdgeDevice().getOrchestrator())
					.findReplica(task.getApplicationID());
			// The IDs are shifted by 3
			task.setRegistry(simulationManager.getServersManager().getDatacenterList().get((int) from - 3));
			// Pull container from another edge device
			scheduleNow(this, NetworkModel.DOWNLOAD_CONTAINER, task);
		}

	}

}
