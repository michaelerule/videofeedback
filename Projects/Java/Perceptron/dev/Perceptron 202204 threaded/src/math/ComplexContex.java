package math;


/**
 *
 * @author mer49
 */
public class ComplexContex { 
    public  final String names; 
    public  final int size; 
    private final complex[] values; 
    public ComplexContex() { names = "abcdefghijklmnopqrstuvwxyz"; values = new complex[26]; size = names.length(); }    
    public ComplexContex(ComplexContex other) { names = other.getNames(); values = other.getValues(); size = names.length(); }    
    public int size() { return size; }    
    public String getNames() { return names; }    
    public complex[] getValues() { 
        complex[] temp = new complex[values.length]; 
        System.arraycopy(names, 0, temp, 0, size); 
        return temp; 
    }    
    public complex getVal(char name) { return values[(int) name - 97]; }    
    public complex get(int index) { return values[index]; }    
    public complex set(int index, complex value) { 
        if (index<0||index>=values.length) {
            throw new IllegalArgumentException(
                "index "+index+"("+((char)index+'a')+") is "
                +"out of bounds for variable set of length "+values.length);
        }
        return (values[index] = value);
    }
    public complex set(char varname, complex value) {return set(varname-'a',value);}
    public static ComplexContex standard() { 
        ComplexContex result = new ComplexContex(); 
        result.set('i', complex.I); 
        result.set('e', complex.E); 
        result.set('p', complex.PI); 
        result.set('f', complex.PHI);
        return result; 
    }
}