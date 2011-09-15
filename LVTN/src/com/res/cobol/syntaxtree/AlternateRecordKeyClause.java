//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;ALTERNATE&gt;
 * nodeToken1 -> &lt;RECORD&gt;
 * nodeOptional -> [ &lt;KEY&gt; ]
 * nodeOptional1 -> [ &lt;IS&gt; ]
 * qualifiedDataName -> QualifiedDataName()
 * nodeOptional2 -> [ PasswordClause() ]
 * nodeOptional3 -> [ [ &lt;WITH&gt; ] &lt;DUPLICATES&gt; ]
 * </PRE>
 */
public class AlternateRecordKeyClause extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public NodeToken nodeToken1;
   public NodeOptional nodeOptional;
   public NodeOptional nodeOptional1;
   public QualifiedDataName qualifiedDataName;
   public NodeOptional nodeOptional2;
   public NodeOptional nodeOptional3;

   public AlternateRecordKeyClause(NodeToken n0, NodeToken n1, NodeOptional n2, NodeOptional n3, QualifiedDataName n4, NodeOptional n5, NodeOptional n6) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      nodeToken1 = n1;
      if ( nodeToken1 != null ) nodeToken1.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeOptional1 = n3;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
      qualifiedDataName = n4;
      if ( qualifiedDataName != null ) qualifiedDataName.setParent(this);
      nodeOptional2 = n5;
      if ( nodeOptional2 != null ) nodeOptional2.setParent(this);
      nodeOptional3 = n6;
      if ( nodeOptional3 != null ) nodeOptional3.setParent(this);
   }

   public AlternateRecordKeyClause() {}

   public void accept(com.res.cobol.visitor.Visitor v) throws Exception {
      v.visit(this);
   }
   public <R,A> R accept(com.res.cobol.visitor.GJVisitor<R,A> v, A argu) throws Exception {
      return v.visit(this,argu);
   }
   public <R> R accept(com.res.cobol.visitor.GJNoArguVisitor<R> v) throws Exception {
      return v.visit(this);
   }
   public <A> void accept(com.res.cobol.visitor.GJVoidVisitor<A> v, A argu) throws Exception {
      v.visit(this,argu);
   }
   public void setParent(Node n) { parent = n; }
   public Node getParent()       { return parent; }
}

