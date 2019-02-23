package by.matrosov.crocoproject.service.dictionary;

import by.matrosov.crocoproject.model.Dictionary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DictionaryService {

    void save(Dictionary dictionary);
    long count();
    Dictionary findByValue(String value);
    List<Dictionary> getAllWords();
}
