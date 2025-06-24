package com.gym.crm.storage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class InMemoryStorage {
    @Value("${storage.init.file.path}")
    private String initDataFilePath;

    private DataFileLoader dataFileLoader;

    @Autowired
    public void setDataFileLoader(DataFileLoader dataFileLoader) {
        this.dataFileLoader = dataFileLoader;
    }

    @PostConstruct
    public void initializeData() {
        dataFileLoader.loadDataFromFile(initDataFilePath);
    }

}
