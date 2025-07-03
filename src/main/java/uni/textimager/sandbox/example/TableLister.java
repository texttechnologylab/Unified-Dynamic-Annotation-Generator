package uni.textimager.sandbox.example;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uni.textimager.sandbox.importer.service.DynamicTableService;

import java.util.List;

@Component
public class TableLister implements ApplicationRunner {
    private final DynamicTableService dynamicTableService;

    public TableLister(DynamicTableService dynamicTableService) {
        this.dynamicTableService = dynamicTableService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> tables = dynamicTableService.listTables();
        tables.forEach(System.out::println);
    }
}
