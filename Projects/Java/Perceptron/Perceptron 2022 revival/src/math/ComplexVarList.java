package math;
/**
 *
 * @author mer49
 */
public class ComplexVarList { 
    
    private final String myVars; 
    private final complex[] myVals; 
    private final int mySize; 

    /**
     *
     */
    public ComplexVarList() { myVars = "abcdefghijklmnopqrstuvwxyz"; myVals = new complex[26]; mySize = myVars.length(); }    

    /**
     *
     * @param otherSet
     */
    public ComplexVarList(ComplexVarList otherSet) { myVars = otherSet.getNames(); myVals = otherSet.getValues(); mySize = myVars.length(); }    

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
        complex[] temp = new complex[myVals.length]; 
        System.arraycopy(myVars, 0, temp, 0, mySize); 
        return temp; 
    }    

    /**
     *
     * @param name
     * @return
     */
    public complex getVal(char name) { return myVals[(int) name - 97]; }    

    /**
     *
     * @param index
     * @return
     */
    public complex get(int index) { return myVals[index]; }    

    /**
     *
     * @param name
     * @param value
     * @return
     */
    public complex setVal(char name, complex value) { return (myVals[(int) name - 97] = value); }  
    
    /**
     *
     * @param index
     * @param value
     * @return
     */
    public complex set(int index, complex value) { return (myVals[index] = value); }    

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