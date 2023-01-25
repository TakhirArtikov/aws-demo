package com.example.demo.util;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageValidator{

    private final Pattern pattern;

    private static final String IMAGE_PATTERN =
            "([^\\s]+(\\.(?i)(jpg|jpeg|png|gif|bmp))$)";

    public ImageValidator(){
        pattern = Pattern.compile(IMAGE_PATTERN);
    }

    /**
     * Validate image with regular expression
     * @param image image for validation
     * @return true valid image, false invalid image
     */
    public boolean validate(final String image){
        Matcher matcher = pattern.matcher(image);
        return matcher.matches();
    }

    public String getImageFileExtension(final String image){
        return Optional.ofNullable(image)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(image.lastIndexOf(".") + 1)).orElse("");
    }
}
