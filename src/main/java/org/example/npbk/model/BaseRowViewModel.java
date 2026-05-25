package org.example.npbk.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class BaseRowViewModel {
    private final ObjectProperty<Long> id = new SimpleObjectProperty<>();
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final ObjectProperty<ValidationState> validationState = new SimpleObjectProperty<>(ValidationState.OK);
    private final ObservableList<String> validationMessages = FXCollections.observableArrayList();

    public ObjectProperty<Long> idProperty() { return id; }
    public Long getId() { return id.get(); }
    public void setId(Long value) { id.set(value); }

    public BooleanProperty dirtyProperty() { return dirty; }
    public boolean isDirty() { return dirty.get(); }
    public void setDirty(boolean value) { dirty.set(value); }

    public ObjectProperty<ValidationState> validationStateProperty() { return validationState; }
    public ValidationState getValidationState() { return validationState.get(); }
    public void setValidationState(ValidationState value) { validationState.set(value == null ? ValidationState.OK : value); }

    public ObservableList<String> getValidationMessages() { return validationMessages; }
    public boolean hasErrors() { return validationState.get() == ValidationState.ERROR; }

    @SuppressWarnings("unchecked")
    protected void markDirtyOnChange(Property<?>... properties) {
        for (Property<?> property : properties) {
            ((Property<Object>) property).addListener((obs, oldValue, newValue) -> dirty.set(true));
        }
    }
}
