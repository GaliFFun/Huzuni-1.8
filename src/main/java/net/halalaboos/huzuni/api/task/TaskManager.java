package net.halalaboos.huzuni.api.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.halalaboos.huzuni.api.node.Node;
import net.halalaboos.huzuni.api.node.attribute.Nameable;
import net.halalaboos.mcwrapper.api.event.player.PreMotionUpdateEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages tasks which can be chosen to run based on priority.
 * */
public class TaskManager <T extends Task> extends Node {
	
	private final List<String> taskHolders = new ArrayList<String>();
	
	private String currentTaskHolder = null;
	
	private T currentTask = null;
	
	public TaskManager(String name, String description) {
		super(name, description);
	}

	public void onPreUpdate(PreMotionUpdateEvent event) {
		if (!event.isCancelled() && currentTask != null) {
			currentTask.onPreUpdate();
		}
	}

	public void onPostUpdate() {
		if (currentTask != null) {
			currentTask.onPostUpdate();
		}
	}

	public T getCurrentTask() {
		return currentTask;
	}

	/**
	 * @return True if the task was selected.
	 * */
	public boolean requestTask(Nameable taskHolder, T task) {
		return this.requestTask(taskHolder.getName(), task);
	}

	/**
	 * @return True if the task was selected.
	 * */
	public boolean requestTask(String taskHolder, T task) {
		if (hasTask() && currentTask == task && currentTaskHolder == taskHolder) {
			return true;
		}
		if (hasTask()) {
			if (hasPriority(taskHolder)) {
				currentTask.onTaskCancelled();
				currentTask.setRunning(false);
				currentTaskHolder = taskHolder;
				currentTask = task;
				currentTask.setRunning(true);
			} else {
				return false;
			}
		} else {
			currentTaskHolder = taskHolder;
			this.currentTask = task;
			this.currentTask.setRunning(true);
		}
		
		return true;
	}

	/**
	 * Withdraws the given task from running. Nothing will occur if the task is not running.
	 * */
	public void withdrawTask(T task) {
		if (currentTask == task) {
			currentTask.setRunning(false);
			currentTask = null;
			currentTaskHolder = null;
		}
	}

	/**
     * @return True if there is a task currently present.
     * */
	public boolean hasTask() {
		return currentTask != null && currentTaskHolder != null;
	}

	/**
     * @return True if the task holder will have highest priotity.
     * */
	public boolean hasPriority(Nameable taskHolder) {
		return this.hasPriority(taskHolder.getName());
	}

	/**
     * @return True if the task holder will have highest priority.
     * */
	public boolean hasPriority(String taskHolder) {
		return !hasTask() || isLessThan(taskHolder, currentTaskHolder);
	}

	/**
     * @return True if the first task holder has a higher priority than the other task holder.
     * */
	private boolean isLessThan(String taskHolder, String otherTaskHolder) {
		return taskHolders.indexOf(taskHolder) <= taskHolders.indexOf(otherTaskHolder);
	}

	/**
     * Registers the task holder within the the task holders list. This will default place the task holder at the lowest priority.
     * */
	public void registerTaskHolder(Nameable taskHolder) {
		this.registerTaskHolder(taskHolder.getName());
	}

	/**
     * Registers the task holder within the the task holders list. This will default place the task holder at the lowest priority.
     * */
	public void registerTaskHolder(String taskHolder) {
		if (taskHolders.isEmpty())
			taskHolders.add(taskHolder);
		else
			taskHolders.add(taskHolders.size(), taskHolder);
	}

	public List<String> getTaskHolders() {
		return taskHolders;
	}

	/**
	 * Cancels the current task, if it exists.
	 * */
	public void cancelTask() {
		if (hasTask()) {
			currentTask.onTaskCancelled();
			currentTask.setRunning(false);
			currentTask = null;
			currentTaskHolder = null;
		}
	}

	@Override
	public boolean hasNode(JsonObject json) {
		return json.getAsJsonArray(getName()) != null;
	}

	@Override
	public void save(JsonObject json) throws IOException {
		super.save(json);
		JsonArray array = new JsonArray();
		for (String taskHolder : taskHolders) {
			JsonObject taskObject = new JsonObject();
			taskObject.addProperty("name", taskHolder);
			array.add(taskObject);
		}
		json.add(getName(), array);
	}

	@Override
	public void load(JsonObject json) throws IOException {
		super.load(json);
		if (hasNode(json)) {
			taskHolders.clear();
			JsonArray objects = json.getAsJsonArray(getName());
			for (int i = 0; i < objects.size(); i++) {
				JsonObject itemObject = (JsonObject) objects.get(i);
				String taskHolder = itemObject.get("name").getAsString();
				taskHolders.add(taskHolder);
			}
		}
	}
}
