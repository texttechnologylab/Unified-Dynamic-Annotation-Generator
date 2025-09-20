package uni.textimager.sandbox.api.charts;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChartRegistry {
    private final Map<String, ChartHandler> handlers;

    public ChartRegistry(List<ChartHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ChartHandler::getName, h -> h));
    }

    public ChartHandler get(String shape) {
        return handlers.get(shape);
    }

    public boolean has(String shape) {
        return handlers.containsKey(shape);
    }
}
