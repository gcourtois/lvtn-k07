//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeOptional -> [ &lt;USAGE&gt; [ &lt;IS&gt; ] ]
 * nodeChoice -> ( &lt;BINARY&gt; | &lt;COMP&gt; | &lt;COMP_1&gt; | &lt;COMP_2&gt; | &lt;COMP_3&gt; | &lt;COMP_4&gt; | &lt;COMP_5&gt; | &lt;COMPUTATIONAL&gt; | &lt;COMPUTATIONAL_1&gt; | &lt;COMPUTATIONAL_2&gt; | &lt;COMPUTATIONAL_3&gt; | &lt;COMPUTATIONAL_4&gt; | &lt;COMPUTATIONAL_5&gt; | &lt;DISPLAY&gt; | &lt;DISPLAY_1&gt; | &lt;INDEX&gt; | &lt;PACKED_DECIMAL&gt; | &lt;POINTER&gt; | &lt;FUNCTION_POINTER&gt; | &lt;PROCEDURE_POINTER&gt; | &lt;OBJECT&gt; &lt;REFERENCE&gt; DataName() )
 * </PRE>
 */
public class DataUsageClause extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeOptional nodeOptional;
   public NodeChoice nodeChoice;

   public DataUsageClause(NodeOptional n0, NodeChoice n1) {
      nodeOptional = n0;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeChoice = n1;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public DataUsageClause() {}

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

