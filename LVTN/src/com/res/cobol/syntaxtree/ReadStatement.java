//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeToken -> &lt;READ&gt;
 * fileName -> FileName()
 * nodeOptional -> [ &lt;NEXT&gt; ]
 * nodeOptional1 -> [ &lt;RECORD&gt; ]
 * nodeOptional2 -> [ &lt;INTO&gt; Identifier() ]
 * nodeOptional3 -> [ &lt;KEY&gt; [ &lt;IS&gt; ] QualifiedDataName() ]
 * nodeOptional4 -> [ &lt;INVALID&gt; [ &lt;KEY&gt; ] StatementList() ]
 * nodeOptional5 -> [ &lt;NOT&gt; &lt;INVALID&gt; [ &lt;KEY&gt; ] StatementList() ]
 * nodeOptional6 -> [ [ &lt;AT&gt; ] &lt;END&gt; StatementList() ]
 * nodeOptional7 -> [ &lt;NOT&gt; [ &lt;AT&gt; ] &lt;END&gt; StatementList() ]
 * nodeOptional8 -> [ &lt;END_READ&gt; ]
 * </PRE>
 */
public class ReadStatement extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeToken nodeToken;
   public FileName fileName;
   public NodeOptional nodeOptional;
   public NodeOptional nodeOptional1;
   public NodeOptional nodeOptional2;
   public NodeOptional nodeOptional3;
   public NodeOptional nodeOptional4;
   public NodeOptional nodeOptional5;
   public NodeOptional nodeOptional6;
   public NodeOptional nodeOptional7;
   public NodeOptional nodeOptional8;

   public ReadStatement(NodeToken n0, FileName n1, NodeOptional n2, NodeOptional n3, NodeOptional n4, NodeOptional n5, NodeOptional n6, NodeOptional n7, NodeOptional n8, NodeOptional n9, NodeOptional n10) {
      nodeToken = n0;
      if ( nodeToken != null ) nodeToken.setParent(this);
      fileName = n1;
      if ( fileName != null ) fileName.setParent(this);
      nodeOptional = n2;
      if ( nodeOptional != null ) nodeOptional.setParent(this);
      nodeOptional1 = n3;
      if ( nodeOptional1 != null ) nodeOptional1.setParent(this);
      nodeOptional2 = n4;
      if ( nodeOptional2 != null ) nodeOptional2.setParent(this);
      nodeOptional3 = n5;
      if ( nodeOptional3 != null ) nodeOptional3.setParent(this);
      nodeOptional4 = n6;
      if ( nodeOptional4 != null ) nodeOptional4.setParent(this);
      nodeOptional5 = n7;
      if ( nodeOptional5 != null ) nodeOptional5.setParent(this);
      nodeOptional6 = n8;
      if ( nodeOptional6 != null ) nodeOptional6.setParent(this);
      nodeOptional7 = n9;
      if ( nodeOptional7 != null ) nodeOptional7.setParent(this);
      nodeOptional8 = n10;
      if ( nodeOptional8 != null ) nodeOptional8.setParent(this);
   }

   public ReadStatement() {}

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

