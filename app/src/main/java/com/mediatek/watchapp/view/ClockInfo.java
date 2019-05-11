package com.mediatek.watchapp.view;

import android.graphics.drawable.Drawable;
import java.util.List;

public class ClockInfo {
    private String angle;
    private String arraytype;
    private String centerX;
    private String centerY;
    private String color;
    private String colorarray;
    private String direction;
    private String mulrotate;
    private String name;
    private Drawable namepng;
    private List<Num> nums;
    private String radius;
    private String rotate;
    private String rotatemode;
    private String startAngle;
    private String textcolor;
    private String textsize;
    private String width;

    public class Num {
        private Drawable numDrawable;

        public Drawable getNumDrawable() {
            return this.numDrawable;
        }

        public void setNumDrawable(Drawable numDrawable) {
            this.numDrawable = numDrawable;
        }
    }

    public String getRotatemode() {
        return this.rotatemode;
    }

    public void setRotatemode(String rotatemode) {
        this.rotatemode = rotatemode;
    }

    public String getRadius() {
        return this.radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getWidth() {
        return this.width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Drawable getNamepng() {
        return this.namepng;
    }

    public void setNamepng(Drawable namepng) {
        this.namepng = namepng;
    }

    public String getRotate() {
        return this.rotate;
    }

    public void setRotate(String rotate) {
        this.rotate = rotate;
    }

    public String getAngle() {
        return this.angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    public String getMulrotate() {
        return this.mulrotate;
    }

    public void setMulrotate(String mulrotate) {
        this.mulrotate = mulrotate;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStartAngle() {
        return this.startAngle;
    }

    public void setStartAngle(String startAngle) {
        this.startAngle = startAngle;
    }

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getTextsize() {
        return this.textsize;
    }

    public void setTextsize(String textsize) {
        this.textsize = textsize;
    }

    public void setTextcolor(String textcolor) {
        this.textcolor = textcolor;
    }

    public List<Num> getNums() {
        return this.nums;
    }

    public void setNums(List<Num> nums) {
        this.nums = nums;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCenterX() {
        return this.centerX;
    }

    public void setCenterX(String centerX) {
        this.centerX = centerX;
    }

    public String getCenterY() {
        return this.centerY;
    }

    public void setCenterY(String centerY) {
        this.centerY = centerY;
    }

    public String getArraytype() {
        return this.arraytype;
    }

    public void setArraytype(String arraytype) {
        this.arraytype = arraytype;
    }

    public String getColorArray() {
        return this.colorarray;
    }

    public void setColorArray(String colorarray) {
        this.colorarray = colorarray;
    }
}
