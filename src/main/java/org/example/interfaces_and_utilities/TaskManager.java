package org.example.interfaces_and_utilities;

import org.example.tasks_types.Epic;
import org.example.tasks_types.Status;
import org.example.tasks_types.Subtask;
import org.example.tasks_types.Task;

import java.util.List;

public interface TaskManager {
    /**
     * Создание задачи. Сам объект должен передаваться в качестве параметра.
     *
     * @return
     */
    long createTask(Task task);

    /**
     * Создание эпика. Сам объект должен передаваться в качестве параметра.
     *
     * @return
     */
    long createTask(Epic epic);

    /**
     * Создание подзадачи. Сам объект должен передаваться в качестве параметра.
     *
     * @return
     */
    long createTask(Subtask subtask);

    /**
     * Получение списка всех задач.
     */
    List<Task> getListOfTask();

    /**
     * Получение списка всех эпиков.
     */
    List<Task> getListOfEpic();

    /**
     * Получение списка всех подзадач.
     */
    List<Task> getListOfSubtask();

    /**
     * Получение списка задач по приоритетам.
     */
    List<Task> getPrioritizedTasks();

    /**
     * Удаление всех задач.
     */
    void clearListOfTask();

    /**
     * Удаление всех эпиков.
     */
    void clearListOfEpic();

    /**
     * Удаление всех подзадач.
     */
    void clearListOfSubtask();

    /**
     * Получение задачи по идентификатору.
     *
     * @return
     */
    Task getTaskByID(long numberID);

    /**
     * Получение задачи по идентификатору без истории,для внутреннего пользования.
     */
    Task getTaskByIdWithoutStory(long numberId);

    /**
     * Получение эпика по идентификатору.
     *
     * @return
     */
    Epic getEpicByID(long numberID);

    /**
     * Получение эпика по идентификатору, без истории для внутреннего пользования.
     */
    Epic getEpicByIdWithoutStory(long numberId);

    /**
     * Получение подзадачи по идентификатору.
     *
     * @return
     */
    Subtask getSubtaskByID(long numberID);

    /**
     * Получение подзадачи по идентификатору, без истории для внутреннего пользования.
     */
    Subtask getSubtaskByIdWithoutStory(long numberId);

    /**
     * Обновление задачи.
     */
    void updateTask(Task task);

    /**
     * Обновление эпика.
     */
    void updateEpic(Epic epic);

    /**
     * Обновление подзадачи.
     */
    void updateSubtask(Subtask subtask);

    /**
     * Удаление задачи по идентификатору.
     */
    void deleteTaskForID(long numberID);

    /**
     * Удаление эпика по идентификатору.
     */
    void deleteEpicForID(long numberID);

    /**
     * Удаление подзадачи по идентификатору.
     */
    void deleteSubtaskForID(long numberID);

    /**
     * Установка статуса для задачи
     */
    Task setStatusForTask(Task task, Status status);

    /**
     * Установка статуса для подзадачи
     */
    Subtask setStatusForSubtask(Subtask subtask, Status status);

    /**
     * Расчет статуса для эпиков
     */
    void setStatusForEpic(long numberEpicID);

    /**
     * Получаем объект для получения и добавления истории
     */
    HistoryManager getHistoryManager();
}