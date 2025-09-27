

   
/* Apache UIMA v3 - First created by JCasGen Sat Sep 27 08:52:26 CEST 2025 */

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
 * Updated by JCasGen Sat Sep 27 08:52:26 CEST 2025
 * XML source: C:/Users/Philipp/Uni/Master/4_Semester_SS_25/DLTI/project/dlti-dynamic-visualization/target/jcasgen/typesystem.xml
 * @generated */
public class Rede extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.uni.type.Rede";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Rede.class);
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
   
  public final static String _FeatName_redeNr = "redeNr";
  public final static String _FeatName_redeId = "redeId";
  public final static String _FeatName_tagesordnungspunkt = "tagesordnungspunkt";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_redeNr = TypeSystemImpl.createCallSite(Rede.class, "redeNr");
  private final static MethodHandle _FH_redeNr = _FC_redeNr.dynamicInvoker();
  private final static CallSite _FC_redeId = TypeSystemImpl.createCallSite(Rede.class, "redeId");
  private final static MethodHandle _FH_redeId = _FC_redeId.dynamicInvoker();
  private final static CallSite _FC_tagesordnungspunkt = TypeSystemImpl.createCallSite(Rede.class, "tagesordnungspunkt");
  private final static MethodHandle _FH_tagesordnungspunkt = _FC_tagesordnungspunkt.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected Rede() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Rede(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Rede(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Rede(JCas jcas, int begin, int end) {
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
  //* Feature: redeNr

  /** getter for redeNr - gets 
   * @generated
   * @return value of the feature 
   */
  public int getRedeNr() { 
    return _getIntValueNc(wrapGetIntCatchException(_FH_redeNr));
  }
    
  /** setter for redeNr - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRedeNr(int v) {
    _setIntValueNfc(wrapGetIntCatchException(_FH_redeNr), v);
  }    
    
   
    
  //*--------------*
  //* Feature: redeId

  /** getter for redeId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRedeId() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_redeId));
  }
    
  /** setter for redeId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRedeId(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_redeId), v);
  }    
    
   
    
  //*--------------*
  //* Feature: tagesordnungspunkt

  /** getter for tagesordnungspunkt - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTagesordnungspunkt() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_tagesordnungspunkt));
  }
    
  /** setter for tagesordnungspunkt - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTagesordnungspunkt(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_tagesordnungspunkt), v);
  }    
    
  }

    