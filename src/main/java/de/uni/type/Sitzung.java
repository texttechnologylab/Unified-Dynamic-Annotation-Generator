

   
/* Apache UIMA v3 - First created by JCasGen Tue Oct 07 10:22:01 CEST 2025 */

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
 * Updated by JCasGen Tue Oct 07 10:22:01 CEST 2025
 * XML source: M:/Data/Programming/Java/AppProjects/dlti-dynamic-visualization/target/jcasgen/typesystem.xml
 * @generated */
public class Sitzung extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.uni.type.Sitzung";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Sitzung.class);
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
   
  public final static String _FeatName_sitzungsNr = "sitzungsNr";
  public final static String _FeatName_date = "date";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_sitzungsNr = TypeSystemImpl.createCallSite(Sitzung.class, "sitzungsNr");
  private final static MethodHandle _FH_sitzungsNr = _FC_sitzungsNr.dynamicInvoker();
  private final static CallSite _FC_date = TypeSystemImpl.createCallSite(Sitzung.class, "date");
  private final static MethodHandle _FH_date = _FC_date.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected Sitzung() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Sitzung(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Sitzung(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Sitzung(JCas jcas, int begin, int end) {
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
  //* Feature: sitzungsNr

  /** getter for sitzungsNr - gets 
   * @generated
   * @return value of the feature 
   */
  public int getSitzungsNr() { 
    return _getIntValueNc(wrapGetIntCatchException(_FH_sitzungsNr));
  }
    
  /** setter for sitzungsNr - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSitzungsNr(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_sitzungsNr), v);
  }    
    
   
    
  //*--------------*
  //* Feature: date

  /** getter for date - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDate() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_date));
  }
    
  /** setter for date - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDate(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_date), v);
  }    
    
  }

    