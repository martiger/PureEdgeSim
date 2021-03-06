package com.mechalikh.pureedgesim;

import java.util.ArrayList;
import java.util.List;

import com.mechalikh.pureedgesim.DataCentersManager.DataCenter;
import com.mechalikh.pureedgesim.DataCentersManager.DefaultDataCenter;
import com.mechalikh.pureedgesim.DataCentersManager.DefaultEnergyModel;
import com.mechalikh.pureedgesim.DataCentersManager.EnergyModel;
import com.mechalikh.pureedgesim.LocationManager.DefaultMobilityModel;
import com.mechalikh.pureedgesim.LocationManager.Mobility;
import com.mechalikh.pureedgesim.Network.DefaultNetworkModel;
import com.mechalikh.pureedgesim.Network.NetworkModel;
import com.mechalikh.pureedgesim.ScenarioManager.Scenario;
import com.mechalikh.pureedgesim.SimulationManager.SimLog;
import com.mechalikh.pureedgesim.TasksGenerator.DefaultTasksGenerator;
import com.mechalikh.pureedgesim.TasksGenerator.TasksGenerator;
import com.mechalikh.pureedgesim.TasksOrchestration.DefaultEdgeOrchestrator;
import com.mechalikh.pureedgesim.TasksOrchestration.Orchestrator;

public abstract class MainApplicationAbstract {
	public static enum Files {
		SIMULATION_PARAMETERS, APPLICATIONS_FILE, EDGE_DATACENTERS_FILE, EDGE_DEVICES_FILE, CLOUD_FILE
	}

	// Simulation scenario files
	protected static String simConfigfile = "PureEdgeSim/settings/simulation_parameters.properties";
	protected static String applicationsFile = "PureEdgeSim/settings/applications.xml";
	protected static String edgeDataCentersFile = "PureEdgeSim/settings/edge_datacenters.xml";
	protected static String edgeDevicesFile = "PureEdgeSim/settings/edge_devices.xml";
	protected static String cloudFile = "PureEdgeSim/settings/cloud.xml";
	protected static String outputFolder = "PureEdgeSim/output/";

	// Parallel simulation Parameters
	protected int fromIteration;
	protected int step = 1;
	protected static int cpuCores;
	protected static List<Scenario> Iterations = new ArrayList<>();
	protected static Class<? extends Mobility> mobilityManager = DefaultMobilityModel.class;
	protected static Class<? extends DataCenter> edgedatacenter = DefaultDataCenter.class;
	protected static Class<? extends TasksGenerator> tasksGenerator = DefaultTasksGenerator.class;
	protected static Class<? extends Orchestrator> orchestrator = DefaultEdgeOrchestrator.class;
	protected static Class<? extends EnergyModel> energyModel = DefaultEnergyModel.class;
	protected static Class<? extends NetworkModel> networkModel = DefaultNetworkModel.class;

	public static String getOutputFolder() {
		return outputFolder;
	}

	protected static void setCustomEdgeDataCenters(Class<? extends DataCenter> edgedatacenter2) {
		edgedatacenter = edgedatacenter2;
	}

	protected static void setCustomTasksGenerator(Class<? extends TasksGenerator> tasksGenerator2) {
		tasksGenerator = tasksGenerator2;
	}

	protected static void setCustomEdgeOrchestrator(Class<? extends Orchestrator> orchestrator2) {
		orchestrator = orchestrator2;
	}

	protected static void setCustomMobilityModel(Class<? extends Mobility> mobilityManager2) {
		mobilityManager = mobilityManager2;
	}

	protected static void setCustomEnergyModel(Class<? extends EnergyModel> energyModel2) {
		energyModel = energyModel2;
	}

	protected static void setCustomNetworkModel(Class<? extends NetworkModel> networkModel2) {
		networkModel = networkModel2;
	}

	protected static void setCustomOutputFolder(String outputFolder2) {
		outputFolder = outputFolder2;
	}

	protected static void setCustomSettingsFolder(String settingsFolder) {
		setCustomFilePath(settingsFolder + "simulation_parameters.properties", Files.SIMULATION_PARAMETERS);
		setCustomFilePath(settingsFolder + "applications.xml", Files.APPLICATIONS_FILE);
		setCustomFilePath(settingsFolder + "edge_datacenters.xml", Files.EDGE_DATACENTERS_FILE);
		setCustomFilePath(settingsFolder + "edge_devices.xml", Files.EDGE_DEVICES_FILE);
		setCustomFilePath(settingsFolder + "cloud.xml", Files.CLOUD_FILE);
	}

	protected static void setCustomFilePath(String path, Files file) {
		switch (file) {
		case SIMULATION_PARAMETERS:
			simConfigfile = path;
			break;
		case APPLICATIONS_FILE:
			applicationsFile = path;
			break;
		case EDGE_DATACENTERS_FILE:
			edgeDataCentersFile = path;
			break;
		case EDGE_DEVICES_FILE:
			edgeDevicesFile = path;
			break;
		case CLOUD_FILE:
			cloudFile = path;
			break;
		default:
			SimLog.println("Unknown file type");
			break;
		}
	}
	
	
}
