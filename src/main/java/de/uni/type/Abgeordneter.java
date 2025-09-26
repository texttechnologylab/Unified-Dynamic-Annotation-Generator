

   
/* Apache UIMA v3 - First created by JCasGen Fri Sep 26 19:13:34 CEST 2025 */

package de.uni.type;
 

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Sep 26 19:13:34 CEST 2025
 * XML source: C:/Users/Philipp/Uni/Master/4_Semester_SS_25/DLTI/project/dlti-dynamic-visualization/target/jcasgen/typesystem.xml
 * @generated */
public class Abgeordneter extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.uni.type.Abgeordneter";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Abgeordneter.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public final static String _FeatName_id = "id";
  public final static String _FeatName_titel = "titel";
  public final static String _FeatName_vorname = "vorname";
  public final static String _FeatName_nachname = "nachname";
  public final static String _FeatName_fraktion = "fraktion";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_id = TypeSystemImpl.createCallSite(Abgeordneter.class, "id");
  private final static MethodHandle _FH_id = _FC_id.dynamicInvoker();
  private final static CallSite _FC_titel = TypeSystemImpl.createCallSite(Abgeordneter.class, "titel");
  private final static MethodHandle _FH_titel = _FC_titel.dynamicInvoker();
  private final static CallSite _FC_vorname = TypeSystemImpl.createCallSite(Abgeordneter.class, "vorname");
  private final static MethodHandle _FH_vorname = _FC_vorname.dynamicInvoker();
  private final static CallSite _FC_nachname = TypeSystemImpl.createCallSite(Abgeordneter.class, "nachname");
  private final static MethodHandle _FH_nachname = _FC_nachname.dynamicInvoker();
  private final static CallSite _FC_fraktion = TypeSystemImpl.createCallSite(Abgeordneter.class, "fraktion");
  private final static MethodHandle _FH_fraktion = _FC_fraktion.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected Abgeordneter() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Abgeordneter(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Abgeordneter(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Abgeordneter(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated
   * @return value of the feature 
   */
  public String getId() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_id));
  }
    
  /** setter for id - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_id), v);
  }    
    
   
    
  //*--------------*
  //* Feature: titel

  /** getter for titel - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTitel() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_titel));
  }
    
  /** setter for titel - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTitel(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_titel), v);
  }    
    
   
    
  //*--------------*
  //* Feature: vorname

  /** getter for vorname - gets 
   * @generated
   * @return value of the feature 
   */
  public String getVorname() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_vorname));
  }
    
  /** setter for vorname - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setVorname(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_vorname), v);
  }    
    
   
    
  //*--------------*
  //* Feature: nachname

  /** getter for nachname - gets 
   * @generated
   * @return value of the feature 
   */
  public String getNachname() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_nachname));
  }
    
  /** setter for nachname - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNachname(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_nachname), v);
  }    
    
   
    
  //*--------------*
  //* Feature: fraktion

  /** getter for fraktion - gets 
   * @generated
   * @return value of the feature 
   */
  public String getFraktion() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_fraktion));
  }
    
  /** setter for fraktion - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFraktion(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_fraktion), v);
  }    
    
  }

    