//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeOptional -> [ &lt;COMMACHAR&gt; ]
 * nodeChoice -> ( DataPictureClause() | DataValueClause() | DataUsageClause() | DataRedefinesClause() | DataExternalClause() | DataGlobalClause() | DataSignClause() | DataOccursClause() | DataSynchronizedClause() | DataJustifiedClause() | DataBlankWhenZeroClause() )
 * nodeOptional1 -> [ &lt;COMMACHAR&gt; ]
 * </PRE>
 */
public class DataDescriptionEntryClause extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeOptional nodeOptional;
   public NodeChoice nodeChoice;
   public NodeOptional nodeOptional1;

   public DataDescriptionEntryClause(NodeOptional n0, NodeChoice n1, NodeOptional n2) {
      nodeOptional = n0;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeChoice = n1;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
      nodeOptional1 = n2;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
   }

   public DataDescriptionEntryClause() {}

   public void accept(com.res.cobol.visitor.Visitor v) throws Exception {
       v.visit(this);
   }

   public <R, A> R accept(com.res.cobol.visitor.GJVisitor<R, A> v, A argu)
           throws Exception {
       return v.visit(this, argu);
   }

   public <R> R accept(com.res.cobol.visitor.GJNoArguVisitor<R> v)
           throws Exception {
       return v.visit(this);
   }

   public <A> void accept(com.res.cobol.visitor.GJVoidVisitor<A> v, A argu)
           throws Exception {
       v.visit(this, argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

