/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package image;

/**
 * Abstract class that retrieves pixel data from a texture.
 * @author mer49
 */
public abstract class AbstractSampler {
    /** 
     * Retrieve color data at given (x,y) coordinate.
     * @param x
     * @param y
     * @return RRGGBB packed color data
     */
    public abstract int it(int x, int y);
}
