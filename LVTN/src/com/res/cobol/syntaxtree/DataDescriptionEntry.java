//
// Generated by JTB 1.3.2
//

package com.res.cobol.syntaxtree;

/**
 * Grammar production:
 * <PRE>
 * nodeChoice -> ( LevelNumber() ( DataName() | &lt;FILLER&gt; )? ( DataDescriptionEntryClause() )* &lt;DOT&gt; | &lt;LEVEL_66&gt; DataName() RenamesClause() &lt;DOT&gt; | &lt;LEVEL_77&gt; DataName() ( DataDescriptionEntryClause() )* &lt;DOT&gt; | &lt;LEVEL_78&gt; ConditionName() ConditionValueClause() &lt;DOT&gt; | &lt;LEVEL_88&gt; ConditionName() ConditionValueClause() &lt;DOT&gt; | ( &lt;EXEC&gt; | &lt;EXECUTE&gt; ) &lt;K_SQL&gt; ( &lt;K_INCLUDE&gt; ( &lt;S_IDENTIFIER&gt; | &lt;S_QUOTED_IDENTIFIER&gt; ) &lt;DOT&gt; | &lt;K_BEGIN&gt; &lt;K_DECLARE&gt; &lt;K_SECTION&gt; &lt;END_EXEC&gt; &lt;DOT&gt; | &lt;K_END&gt; &lt;K_DECLARE&gt; &lt;K_SECTION&gt; &lt;END_EXEC&gt; &lt;DOT&gt; | DeclareCursorStatement() &lt;END_EXEC&gt; &lt;DOT&gt; ) )
 * </PRE>
 */
public class DataDescriptionEntry extends com.res.cobol.RESNode implements Node {
   private Node parent;
   public NodeChoice nodeChoice;

   public DataDescriptionEntry(NodeChoice n0) {
      nodeChoice = n0;
      if ( nodeChoice != null ) nodeChoice.setParent(this);
   }

   public DataDescriptionEntry() {}

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

