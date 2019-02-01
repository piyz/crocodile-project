package by.matrosov.crocoproject.repository;

import by.matrosov.crocoproject.model.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictionaryRepository extends JpaRepository<Dictionary,Long> {
    Dictionary findByValue(String value);
}
