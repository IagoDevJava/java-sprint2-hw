package org.example.managers_types;

import org.example.interfaces_and_utilities.HistoryManager;
import org.example.interfaces_and_utilities.Managers;
import org.example.interfaces_and_utilities.TaskManager;
import org.example.tasks_types.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    /**
     * История задач.
     */
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    /**
     * Возможность хранить задачи всех типов.
     */
    protected final Map<Long, Task> allTasks = new HashMap<>();
    protected final Map<Long, Epic> allEpicTasks = new HashMap<>();
    protected final Map<Long, Subtask> allSubtasks = new HashMap<>();

    protected final Map<LocalDateTime, Task> prioritizedMapOfTasks = new TreeMap<>();

    protected long createdID = 0L;

    /**
     * Создание задачи. Сам объект должен передаваться в качестве параметра.
     */
    @Override
    public long createTask(Task task) {
        long thisID = creatingID();
        task.setNumberId(thisID);
        allTasks.put(thisID, task);
        task.setStatus(Status.NEW);
        addTasksInPrioritizedList(task);
        return thisID;
    }

    /**
     * Создание эпика. Сам объект должен передаваться в качестве параметра.
     */
    @Override
    public long createTask(Epic epic) {
        long thisID = creatingID();
        epic.setNumberId(thisID);
        epic.setStatus(Status.NEW);

        setStartTimeEpic(epic);
        setEndTimeEpic(epic);
        setDurationEpic(epic);

        allEpicTasks.put(thisID, epic);

        return thisID;
    }

    /**
     * Создание подзадачи. Сам объект должен передаваться в качестве параметра.
     */
    @Override
    public long createTask(Subtask subtask) {
        long thisID = creatingID();
        long epicID = subtask.getMyEpicID();
        if (allEpicTasks.containsKey(epicID)) {
            subtask.setNumberId(thisID);
            allSubtasks.put(thisID, subtask);
            subtask.setStatus(Status.NEW);
            addTasksInPrioritizedList(subtask);

            Epic thisEpic = allEpicTasks.get(epicID);
            thisEpic.getIdsOfSubtasksEpic().add(thisID);
            setStatusForEpic(epicID);
            updateTimeEpic(thisEpic);
        }
        return thisID;
    }

    /**
     * Получение списка всех задач.
     */
    @Override
    public List<Task> getListOfTask() {
        return new ArrayList<>(allTasks.values());
    }

    /**
     * Получение списка всех эпиков.
     */
    @Override
    public List<Task> getListOfEpic() {
        return new ArrayList<>(allEpicTasks.values());
    }

    /**
     * Получение списка всех подзадач.
     */
    @Override
    public List<Task> getListOfSubtask() {
        return new ArrayList<>(allSubtasks.values());
    }

    /**
     * Получение списка задач по приоритетам.
     */
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedMapOfTasks.values());
    }

    /**
     * Удаление всех задач.
     */
    @Override
    public void clearListOfTask() {
        for (Long aLong : allTasks.keySet()) {
            historyManager.remove(aLong);
        }

        for (Task value : allTasks.values()) {
            prioritizedMapOfTasks.remove(value.getStartTime());
        }

        allTasks.clear();
    }

    /**
     * Удаление всех эпиков.
     */
    @Override
    public void clearListOfEpic() {
        for (Long aLong : allEpicTasks.keySet()) {
            historyManager.remove(aLong);
        }

        for (Epic value : allEpicTasks.values()) {
            prioritizedMapOfTasks.remove(value.getStartTime());
        }

        allEpicTasks.clear();
        allSubtasks.clear();
    }

    /**
     * Удаление всех подзадач.
     */
    @Override
    public void clearListOfSubtask() {
        for (Long aLong : allSubtasks.keySet()) {
            historyManager.remove(aLong);
        }

        for (Subtask value : allSubtasks.values()) {
            prioritizedMapOfTasks.remove(value.getStartTime());
        }

        allSubtasks.clear();

        for (Long aLong : allEpicTasks.keySet()) {
            allEpicTasks.get(aLong).getIdsOfSubtasksEpic().clear();
            setStatusForEpic(aLong);
        }
    }

    /**
     * Получение задачи по идентификатору.
     */
    @Override
    public Task getTaskByID(long numberId) {
        Task task = allTasks.get(numberId);
        if (allTasks.containsKey(numberId)) {
            historyManager.add(task);
        }
        return task;
    }

    /**
     * Получение задачи по идентификатору без истории, для внутреннего пользования.
     */
    @Override //метод используется в тестах, поэтому пришлось оставить его публичным
    public Task getTaskByIdWithoutStory(long numberId) {
        return allTasks.get(numberId);
    }

    /**
     * Получение эпика по идентификатору.
     */
    @Override
    public Epic getEpicByID(long numberId) {
        Epic epic = allEpicTasks.get(numberId);
        if (allEpicTasks.containsKey(numberId)) {
            historyManager.add(epic);
        }
        return epic;
    }

    /**
     * Получение эпика по идентификатору, без истории для внутреннего пользования.
     */
    @Override
    public Epic getEpicByIdWithoutStory(long numberId) {
        return allEpicTasks.get(numberId);
    }

    /**
     * Получение подзадачи по идентификатору.
     */
    @Override
    public Subtask getSubtaskByID(long numberId) {
        Subtask subtask = allSubtasks.get(numberId);
        if (allSubtasks.containsKey(numberId)) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    /**
     * Получение подзадачи по идентификатору, без истории для внутреннего пользования.
     */
    @Override
    public Subtask getSubtaskByIdWithoutStory(long numberId) {
        return allSubtasks.get(numberId);
    }

    /**
     * Обновление задачи.
     */
    @Override
    public void updateTask(Task task) {
        final Task savedTask = allTasks.get(task.getNumberId());
        if (checkTasksForIntersectionsByTime(savedTask)) {
            savedTask.setNameTask(task.getNameTask());
            savedTask.setDescription(task.getDescription());
            savedTask.setStatus(Status.NEW);
            savedTask.setStartTime(task.getStartTime());
            savedTask.setDuration(task.getDuration());
            allTasks.put(savedTask.getNumberId(), savedTask);
        }
    }

    /**
     * Обновление эпика.
     */
    @Override
    public void updateEpic(Epic epic) {
        final Epic savedEpic = allEpicTasks.get(epic.getNumberId());
        savedEpic.setNameTask(epic.getNameTask());
        savedEpic.setDescription(epic.getDescription());
        allEpicTasks.put(savedEpic.getNumberId(), savedEpic);
    }

    /**
     * Обновление подзадачи.
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        Long id = subtask.getNumberId();
        final Subtask savedSubtask = allSubtasks.get(id);
        if (checkTasksForIntersectionsByTime(savedSubtask)) {
            savedSubtask.setNameTask(subtask.getNameTask());
            savedSubtask.setDescription(subtask.getDescription());
            savedSubtask.setStatus(Status.NEW);
            savedSubtask.setStartTime(subtask.getStartTime());
            savedSubtask.setDuration(subtask.getDuration());
            allSubtasks.put(id, savedSubtask);
        }
    }

    /**
     * Удаление задачи по идентификатору.
     */
    @Override
    public void deleteTaskForID(long numberId) {
        Task task = allTasks.get(numberId);
        if (task != null) {
            historyManager.getHistory().remove(task);
            allTasks.remove(numberId);
            historyManager.remove(numberId);
        }
    }

    /**
     * Удаление эпика по идентификатору.
     */
    @Override
    public void deleteEpicForID(long numberId) {
        Epic epic = allEpicTasks.get(numberId);
        if (epic != null) {
            for (Long aLong : epic.getIdsOfSubtasksEpic()) {
                allSubtasks.remove(aLong);
            }
            allEpicTasks.remove(numberId);
            historyManager.remove(numberId);
        }
    }

    /**
     * Удаление подзадачи по идентификатору.
     */
    @Override
    public void deleteSubtaskForID(long numberId) {
        Subtask subtask = allSubtasks.get(numberId);
        if (subtask != null) {
            Epic epic = allEpicTasks.get(subtask.getMyEpicID());
            epic.getIdsOfSubtasksEpic().remove(numberId);
            allSubtasks.remove(numberId);
            historyManager.remove(numberId);
        }
    }

    /**
     * Установка статуса для задачи
     */
    @Override
    public Task setStatusForTask(Task task, Status status) {
        if (!(task instanceof Epic)) {
            task.setStatus(status);
            allTasks.put(task.getNumberId(), task);
        }
        return task;
    }

    /**
     * Установка статуса для подзадачи
     */
    @Override
    public Subtask setStatusForSubtask(Subtask subtask, Status status) {
        subtask.setStatus(status);
        allSubtasks.put(subtask.getNumberId(), subtask);

        Epic epic = getEpicByID(subtask.getMyEpicID());
        epic.getIdsOfSubtasksEpic().add(subtask.getNumberId());
        historyManager.remove(epic.getNumberId());
        setStatusForEpic(subtask.getMyEpicID());

        return subtask;
    }

    /**
     * Установка статуса для эпиков
     */
    @Override
    public void setStatusForEpic(long numberEpicID) {
        boolean isStatus = true;

        Epic newEpic = allEpicTasks.get(numberEpicID);
        for (Long aLong : newEpic.getIdsOfSubtasksEpic()) {
            if (allSubtasks.get(aLong).getStatus().equals(Status.IN_PROGRESS)
                    || allSubtasks.get(aLong).getStatus().equals(Status.NEW)) {
                isStatus = false;
            }
        }

        if (isStatus) {
            newEpic.setStatus(Status.DONE);
        } else {
            newEpic.setStatus(Status.IN_PROGRESS);
        }

        allEpicTasks.put(numberEpicID, newEpic);
    }

    /**
     * Установка времени старта для эпиков
     */
    protected LocalDateTime setStartTimeEpic(Epic epic) {
        epic.setStartTime(LocalDateTime.MAX);
        if (!epic.getIdsOfSubtasksEpic().isEmpty()) {
            for (Long idSubtaskThisEpic : epic.getIdsOfSubtasksEpic()) {
                Subtask subtask = getSubtaskByIdWithoutStory(idSubtaskThisEpic);
                LocalDateTime startSubtask = subtask.getStartTime();
                epic.setStartTime(startSubtask);
                if (startSubtask.isBefore(epic.getStartTime())) {
                    epic.setStartTime(startSubtask);
                }
            }
        }
        return epic.getStartTime();
    }

    /**
     * Установка времени окончания для эпиков
     */
    protected LocalDateTime setEndTimeEpic(Epic epic) {
        epic.setEndTime(LocalDateTime.MIN);
        if (!epic.getIdsOfSubtasksEpic().isEmpty()) {
            for (Long idSubtaskThisEpic : epic.getIdsOfSubtasksEpic()) {
                Subtask subtask = getSubtaskByIdWithoutStory(idSubtaskThisEpic);
                LocalDateTime endSubtask = subtask.getEndTime();
                epic.setEndTime(endSubtask);
                if (endSubtask.isAfter(epic.getEndTime())) {
                    epic.setEndTime(endSubtask);
                }
            }
        }
        return epic.getEndTime();
    }

    /**
     * Установка продолжительности для эпиков
     */
    protected Duration setDurationEpic(Epic epic) {
        epic.setDuration(Duration.between(epic.getStartTime(), epic.getEndTime()));
        return epic.getDuration();
    }

    /**
     * Обновление временных меток для эпиков по подзадачам
     */
    protected Duration updateTimeEpic(Epic epic) {
        LocalDateTime startTimeEpic = epic.getStartTime();
        LocalDateTime endTimeEpic = epic.getEndTime();
        Duration durationEpic = epic.getDuration();
        for (Long aLong : epic.getIdsOfSubtasksEpic()) {
            LocalDateTime startTimeSubtask = getSubtaskByIdWithoutStory(aLong).getStartTime();
            LocalDateTime endTimeSubtask = getSubtaskByIdWithoutStory(aLong).getEndTime();

            if (startTimeSubtask.isBefore(startTimeEpic)) {
                epic.setStartTime(startTimeSubtask);
            }
            if (endTimeSubtask.isAfter(endTimeEpic)) {
                epic.setEndTime(endTimeSubtask);
            }
            durationEpic = durationEpic.plus(getSubtaskByIdWithoutStory(aLong).getDuration());
            epic.setDuration(durationEpic);
        }
        return epic.getDuration();
    }

    /**
     * Проверка задач на пересечение во времени.
     */
    private Boolean checkTasksForIntersectionsByTime(Task newTask) {

        if (prioritizedMapOfTasks.isEmpty()) {
            return true;
        }
        for (Task task : prioritizedMapOfTasks.values()) {
            if (task.getNumberId() == newTask.getNumberId()) {
                continue;
            }
            if (!newTask.getEndTime().isAfter(task.getStartTime())) {
                continue;
            }
            if (!newTask.getStartTime().isBefore(task.getEndTime())) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Добавление задач в список по временным меткам.
     */
    private void addTasksInPrioritizedList(Task task) {
        if (!(task instanceof Epic)) {
            if (checkTasksForIntersectionsByTime(task)) {
                prioritizedMapOfTasks.put(task.getStartTime(), task);
            } else {
                System.out.println("Измените время выполнения задачи - " + task.getNumberId());
            }
        }
    }

    /**
     * Получаем объект для получения и добавления истории
     */
    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    /**
     * Создание нового ID
     */
    public long creatingID() {
        return ++createdID;
    }

    /**
     * Получение сгенерированного id
     * */
    public long getCreatedID() {
        return createdID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryTaskManager that = (InMemoryTaskManager) o;
        return createdID == that.createdID
                && Objects.equals(historyManager, that.historyManager)
                && Objects.equals(allTasks, that.allTasks)
                && Objects.equals(allEpicTasks, that.allEpicTasks)
                && Objects.equals(allSubtasks, that.allSubtasks)
                && Objects.equals(prioritizedMapOfTasks, that.prioritizedMapOfTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyManager, allTasks, allEpicTasks, allSubtasks, prioritizedMapOfTasks, createdID);
    }

    @Override
    public String toString() {
        return "InMemoryTaskManager{" +
                "historyManager=" + historyManager +
                ", allTasks=" + allTasks +
                ", allEpicTasks=" + allEpicTasks +
                ", allSubtasks=" + allSubtasks +
                ", prioritizedTasks=" + prioritizedMapOfTasks +
                ", createdID=" + createdID +
                '}';
    }
}