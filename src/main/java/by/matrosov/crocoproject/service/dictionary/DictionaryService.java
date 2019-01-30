package by.matrosov.crocoproject.service.dictionary;

import by.matrosov.crocoproject.model.Dictionary;
import org.springframework.stereotype.Service;

@Service
public interface DictionaryService {
    void save(Dictionary dictionary);
}
