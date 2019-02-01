package by.matrosov.crocoproject.service.dictionary;

import by.matrosov.crocoproject.model.Dictionary;
import by.matrosov.crocoproject.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DictionaryServiceImpl implements DictionaryService{

    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Override
    public void save(Dictionary dictionary) {
        dictionaryRepository.save(dictionary);
    }

    @Override
    public long count() {
        return dictionaryRepository.count();
    }

    @Override
    public Dictionary findByValue(String value) {
        return dictionaryRepository.findByValue(value);
    }
}
