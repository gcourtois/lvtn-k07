//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> &lt;MESSAGE&gt; ( &lt;DATE&gt; | &lt;TIME&gt; ) [ &lt;IS&gt; ] DataName()
 *       | &lt;TEXT&gt; &lt;LENGTH&gt; [ &lt;IS&gt; ] DataName()
 *       | &lt;END&gt; &lt;KEY&gt; [ &lt;IS&gt; ] DataName()
 *       | &lt;STATUS&gt; &lt;KEY&gt; [ &lt;IS&gt; ] DataName()
 *       | [ &lt;SYMBOLIC&gt; ] &lt;TERMINAL&gt; [ &lt;IS&gt; ] DataName()
 * </PRE>
 */
public class CommunicationIOClause extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;

   public CommunicationIOClause(NodeChoice n0) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public CommunicationIOClause() {}

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

