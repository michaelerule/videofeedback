/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author mer49
 */
public class Misc {
    
    /** Clip integer in range
     * 
     * @param x
     * @param low
     * @param hi
     * @return 
     */
    public static int clip(int x,int low, int hi) {
        return x<low? low : x>hi? hi : x;
    }
    public static float clip(float x,float low, float hi) {
        return x<low? low : x>hi? hi : x;
    }
    
    /** Periodically wrap to range
     * 
     * @param n
     * @param m
     * @return 
     */
    public static int wrap(int n, int m) {
        return n < 0 ? m - (-n % m) : n % m;
    }
    public static float wrap(float n, float m) {
        return n < 0 ? m - (-n % m) : n % m;
    }
    public static double wrap(double n, double m) {
        return n < 0 ? m - (-n % m) : n % m;
    }
    
    public static <T,U> void zip(Collection<T> A, Collection<U> B, BiConsumer<T, U> c) {
        Iterator<T> it = A.iterator();
        Iterator<U> iu = B.iterator();
        while (it.hasNext() && iu.hasNext()) {
            c.accept(it.next(), iu.next());
        }
    }
    
    public static <S, T> List<T> map(Collection<S> collection, Function<S, T> mapFunction) {
        return collection.stream().map(mapFunction).collect(Collectors.toList());
    }

    
}