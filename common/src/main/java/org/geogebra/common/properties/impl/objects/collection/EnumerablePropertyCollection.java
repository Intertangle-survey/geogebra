package org.geogebra.common.properties.impl.objects.collection;

import java.util.Collection;

import org.geogebra.common.properties.EnumerableProperty;
import org.geogebra.common.properties.Property;

public class EnumerablePropertyCollection implements EnumerableProperty {

    private Collection<? extends EnumerableProperty> propertyCollection;
    EnumerableProperty property;

    public EnumerablePropertyCollection(Collection<? extends EnumerableProperty> propertyCollection) {
        this.propertyCollection = propertyCollection;
        property = propertyCollection.iterator().next();
    }

    @Override
    public String[] getValues() {
        return property.getValues();
    }

    @Override
    public int getIndex() {
        return property.getIndex();
    }

    @Override
    public void setIndex(int index) {
        for (EnumerableProperty property : propertyCollection) {
            property.setIndex(index);
        }
    }

    @Override
    public String getName() {
        return property.getName();
    }

    @Override
    public boolean isEnabled() {
        boolean isEnabled = true;
        for (Property property : propertyCollection) {
            isEnabled = isEnabled && property.isEnabled();
        }
        return isEnabled;
    }
}