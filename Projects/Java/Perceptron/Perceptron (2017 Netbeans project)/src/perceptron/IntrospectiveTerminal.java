/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package perceptron;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.*;

/**
 *
 * I think it would not be too difficult to transform this into a
 * parser for succing Java syntax typing.
 * 
 * @author mrule
 */
public class IntrospectiveTerminal {

    PrintStream devnull = new PrintStream(new OutputStream(){
        @Override
        public void write(int b) throws IOException {
        }
    });

    PrintStream out = System.out;
    PrintStream debug = System.out;
    PrintStream err = System.err;

    private int cols;
    public void setcols(int c){cols=c;}

    /** Emulation of global scope as a dictionary */
    Map<String,Object> objects = new HashMap<String,Object>();

    public void register(String name, Object o) {
        objects.put(name,o);
    }

    Object tryType(String s, Class c) throws Exception {
        debug.println("try type "+c+" for "+s);
        if (c==String.class) return s;
        if (c==Character.class && s.length()==1)
            return s.charAt(0);
        if (c==Integer.class || c==int.class) {
            if (s.indexOf('x')>=0) {
                String foo = s.substring(s.indexOf('x')+1);
                try{return (int)(Long.parseLong(foo,16));}catch(Exception e){
                e.printStackTrace(err);}
            }
            try{return Integer.parseInt(s);}catch(Exception e){}
        }
        if (c==Double.class  || c==double.class)
            return Double.parseDouble(s);
        if (c==Float.class   || c==float.class)
            return Float.parseFloat(s);
        if (c==Short.class   || c==short.class)
            return Short.parseShort(s);
        if (c==Byte.class    || c==byte.class)
            return Byte.parseByte(s);
        if (c==Boolean.class || c==boolean.class)
            return s.toLowerCase().startsWith("t") || s.toLowerCase().equals("on");
        for (Method M : c.getMethods()) {
            debug.println("Method "+M);
            Class [] params = M.getParameterTypes();
            if (params.length==1 &&
                M.getReturnType()==c &&
                params[0]==String.class &&
                M.getName().toLowerCase().substring(0,5).equals("parse"))
            try {//TODO : verify static
                return M.invoke(null,s);
            } catch(Exception e) {
                e.printStackTrace(err);
            }
        }
        //try to interpret it as an expression ?
        throw new Exception("unable to find parser for "+c);
    }

    Object tryMethod(Object o, Method M, String [] s) throws Exception
    {
        Class [] t = M.getParameterTypes();
        int argc = t.length;
        if (argc!=s.length) {
            //hmm... something more here ?
            throw new Exception("argument length mismatch");
        }
        Object [] a = new Object[argc];
        for (int i=0; i<argc; i++) {
            try {
                a[i]=tryType(s[i],t[i]);
            } catch (Exception e)
            {
                a[i]=tryType(eval(s[i]),t[i]);
            }
        }
        debug.println("trying to invoke "+M+" on "+o+"arguments"+a);
        return M.invoke(o, a);
    }

    private String eval(String s)
    {
        try {
            return math.MathToken.toEquation(s).evaluate(math.ComplexVarList.standard()).toString();
        } catch (Exception e)
        {
            return s+"?";
        }
    }

    String [] rest(String [] args)
    {
        if (args.length==0) return args;
        String [] newargs = new String[args.length-1];
        for (int i=0; i<newargs.length; i++)
            newargs[i]=args[i+1];
        return newargs;
    }

    boolean isMatch(String target, String name, String [] args)
    {
        target=target.toLowerCase();
        name=name.toLowerCase();
        if (args.length==0)
            return name.startsWith(target) ||
                   name.startsWith("get_"+target) ||
                   name.startsWith("get"+target) ;
        return name.startsWith(target) ||
               name.startsWith("set_"+target) ||
               name.startsWith("set"+target) ;
    }
    Object tryCall(Object o, String s, String [] args, String RHS) throws Exception
    {
        if (o==null) return null;
        debug.print("CALLING : "+o+"."+s+"(");
        for (String a : args) debug.print(a+",");
        debug.println(")");
        Class    c = o.getClass();
        Method[] m = c.getDeclaredMethods();
        for (Method M : m) if (isMatch(s,M.getName(),args)) try {
            return tryMethod(o,M,args);
        } catch(Exception e) {
            debug.println("failed to call "+M+" for "+o+" with "+args);
            e.printStackTrace(err);
        }
        //it has failed, try recursing on members ?
        debug.println("Failed to resolve Method "+s+" for "+o);
        String member = s;
        debug.println("looking for "+member+" in "+o);
        Field f = c.getDeclaredField(member);
        Object p = f.get(o);
        if (args.length>0) {
            return tryCall(p,args[0],rest(args),RHS);
        }
        if (RHS!=null) out.println(RHS);
        if (RHS==null) 
            printInfo(p);
        else try {
            f.set(o,tryType(RHS,f.getType()));
        } catch( Exception e ) {
            f.set(o,tryType(eval(RHS),f.getType()));
        }
        return null;
    }

    void printInfo(Object o) {
        if (o==null) return;
        debug.println("TRYING TO PRINT INDEX FOR "+o+"\n");
        //out.println(o.toString());
        int col;
        col=-1;
        int K = cols/3;
        String bbb = "";
        for (int i=0; i<K; i++) bbb+=' ';//.append(" ");

        out.println("\nMethods : ");
        for (Method M : o.getClass().getDeclaredMethods()) if (M.getName().indexOf("$")<1 && Modifier.isPublic(M.getModifiers())){
             if (col++==2) {col=0; out.println();}
            //out.print(M.getName());
            StringBuffer st = new StringBuffer(bbb);
            st.insert(0,M.getName());
            out.print(st.toString().substring(0,K));
        }
        col=0;
        out.println("\nFields : ");
        for (Field M : o.getClass().getDeclaredFields()) if (M.getName().indexOf("$")<1) {
            try {
                Object dummy = M.get(o);
                if (!M.getType().isPrimitive()) {
                    if (col++==2) {col=0; out.println();}
                    StringBuffer st = new StringBuffer(bbb);
                    st.insert(0,M.getName());
                    out.print(st.toString().substring(0,K));
                }
            } catch (Exception e) {}
        }
        out.println("\n");
    }

    void printCall(Object o, String s, String [] args, String RHS) throws Exception {
        Object p = tryCall(o,s,args,RHS);
        if (p!=null) out.println(p.toString());
    }

    public void interpret(String cmd, String RHS) {
        String[] tok = cmd.trim().split("\\s+");
        if (!objects.containsKey(tok[0]))
            out.println(eval(tok[0]));
        Object o = objects.get(tok[0]);
        try {
            if (tok.length>1)
                printCall(o,tok[1],rest(rest(tok)),RHS);
            else {
                printInfo(o);
            }
        } catch(Exception e) {
            out.println("Error : " + e.toString());
            e.printStackTrace(err);
        }
    }

    public static void main(String [] args) throws IOException {
        IntrospectiveTerminal t = new IntrospectiveTerminal();
        t.register("a",new Integer(5));
        t.register("b",true);
        t.register("c",1.0f);
        t.register("d","hello");
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        while (true) {
            System.out.print("> ");
            String s = input.readLine();
            if (s.equals("quit")) exit(0);
            t.interpret(s,null);
        }
    }
}
