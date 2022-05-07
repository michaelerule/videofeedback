package math;


/**
 *
 * @author mer49
 */
public class ComplexVarList { 
    
    private final String myVars; 
    private final complex[] values; 
    private final int mySize; 

    /**
     *
     */
    public ComplexVarList() { myVars = "abcdefghijklmnopqrstuvwxyz"; values = new complex[26]; mySize = myVars.length(); }    

    /**
     *
     * @param otherSet
     */
    public ComplexVarList(ComplexVarList otherSet) { myVars = otherSet.getNames(); values = otherSet.getValues(); mySize = myVars.length(); }    

    /**
     *
     * @return
     */
    public int size() { return mySize; }    

    /**
     *
     * @return
     */
    public String getNames() { return myVars; }    

    /**
     *
     * @return
     */
    public complex[] getValues() { 
        complex[] temp = new complex[values.length]; 
        System.arraycopy(myVars, 0, temp, 0, mySize); 
        return temp; 
    }    

    /**
     *
     * @param name
     * @return
     */
    public complex getVal(char name) { return values[(int) name - 97]; }    

    /**
     *
     * @param index
     * @return
     */
    public complex get(int index) { return values[index]; }    

    /**
     *
     * @param name
     * @param value
     * @return
     */
    public complex setVal(char name, complex value) { return (values[(int) name - 97] = value); }  
    
    /**
     *
     * @param index
     * @param value
     * @return
     */
    public complex set(int index, complex value) { 
        if (index<0||index>=values.length) {
            throw new IllegalArgumentException(
                "index "+index+"("+((char)index+'a')+") is "
                +"out of bounds for variable set of length "+values.length);
        }
        return (values[index] = value);
    }
    public complex set(char c, complex value) {
        return set(c-'a',value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void add(char name, complex value) { setVal(name, value); }    

    /**
     *
     */
    public void fillStandard() { setVal('i', complex.I); setVal('e', complex.E); setVal('p', complex.PI); setVal('f', complex.PHI); }    

    /**
     *
     * @return
     */
    public static ComplexVarList standard() { ComplexVarList result = new ComplexVarList(); result.fillStandard(); return result; }}