package net.halalaboos.huzuni.indev;

import net.halalaboos.huzuni.api.node.impl.Value;
import net.halalaboos.huzuni.indev.gui.Container;
import net.halalaboos.huzuni.indev.gui.FontData;
import net.halalaboos.huzuni.indev.gui.components.Label;
import net.halalaboos.huzuni.indev.gui.components.Slider;
import net.halalaboos.huzuni.indev.gui.layouts.GridLayout;

import java.awt.*;

/**
 * Container which holds the title, description, and slider components that are used to represent value nodes. <br/>
 * Created by Brandon Williams on 2/16/2017.
 */
public class ValueContainer extends Container {

    private final Label title, description;

    private final Slider slider;

    private final Value value;

    public ValueContainer(Value value, FontData titleFont, FontData descriptionFont) {
        super("invisible");
        // Using a grid layout with infinite rows and three columns ensures that the components are stacked upon each other.
        this.setLayout(new GridLayout(GridLayout.INFINITE_LENGTH, 3, 0, 1,1));
        this.setUseLayoutSize(true);
        this.setAutoLayout(true);
        this.value = value;
        this.add(title = new Label("title", value.getName()));
        this.title.setFont(titleFont);
        this.add(description = new Label("description", value.getDescription()));
        this.description.setFont(descriptionFont);
        this.description.setColor(new Color(118, 118, 118));
        this.add(slider = new Slider("slider"));
        // Set the default dimensions of the slider as well as updating it's percentage to reflect the value node.
        this.slider.setSize(150, 12);
        this.slider.setSliderPercentage((value.getValue() - value.getMinValue()) / (value.getMaxValue() - value.getMinValue()));
        this.slider.onValueChange((slider1 -> {
            // Calculate the value the slider's percentage would create (before the minimum value)
            float calculatedValue = (slider.getSliderPercentage() * (value.getMaxValue() - value.getMinValue()));

            // Use the modulus operator to trim the excess value from the slider percentage.
            this.value.setValue(value.getMinValue() + calculatedValue - (value.getIncrementValue() == -1 ? 0 : calculatedValue % value.getIncrementValue()));

            // Update the slider percentage with the values current value, as it may differ if there were excess value.
            this.slider.setSliderPercentage((value.getValue() - value.getMinValue()) / (value.getMaxValue() - value.getMinValue()));

            // Update the title label to coincide with any value change.
            this.title.setText(String.format("%s (%.1f%s)", value.getName(), value.getValue(), value.getCarot()));
            return null;
        }));
        this.layout();
    }

    public Value getValue() {
        return value;
    }

    public Label getTitle() {
        return title;
    }

    public Label getDescription() {
        return description;
    }

    public Slider getSlider() {
        return slider;
    }
}
