package com.sparta.dominic.controller;

import com.sparta.dominic.model.EmployeeDTO;
import com.sparta.dominic.util.DataMigrationLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CSVReader {

    public static void transferToDTOManager(String path, EmployeeDTOManager dtoManager) {
        try (BufferedReader bufferedReader = loadToBufferedReader(path)) {
            bufferedReader.lines()
                    .filter(line -> Character.isDigit(line.charAt(0)))
                    .map(line -> line.split(","))
                    .map(EmployeeDTO::new)
                    .forEach(dtoManager::addEmployee);

        } catch (IOException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static Queue<String[]> loadToStringArrays(String path) {
        Queue<String[]> stringArrayQueue = null;
        try (BufferedReader bufferedReader = loadToBufferedReader(path)) {
            stringArrayQueue = bufferedReader.lines()
                    .filter(line -> Character.isDigit(line.charAt(0)))
                    .map(line -> line.split(","))
                    .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        } catch (IOException e) {
            DataMigrationLogger.getLogger().log(Level.SEVERE, e.getMessage(), e);
            stringArrayQueue = new LinkedBlockingQueue<>();
        }
        return stringArrayQueue;
    }

    public static void transferToDToManager(Queue<String[]> stringArrayQueue, EmployeeDTOManager dtoManager) {
        while (!stringArrayQueue.isEmpty()) {
            synchronized (stringArrayQueue) {
                String[] data = stringArrayQueue.poll();
                if (data != null) {
                    dtoManager.addEmployee(new EmployeeDTO(data));
                } else {
                    break;
                }
            }
        }
    }

    private static BufferedReader loadToBufferedReader(String path) throws IOException {
        return new BufferedReader(new FileReader(path));
    }
}
