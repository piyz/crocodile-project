package by.matrosov.crocoproject.validator;

import by.matrosov.crocoproject.model.Dictionary;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class DictionaryValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return Dictionary.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Dictionary dictionary = (Dictionary) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "value", "NotEmpty");
        if (dictionary.getValue().length() < 3 || dictionary.getValue().length() > 32) {
            errors.rejectValue("value", "Size.Dictionary.value");
        }

        if (!dictionary.getValue().matches("^[а-я]+")){
            errors.rejectValue("value", "Characters.Dictionary.value");
        }
    }
}
