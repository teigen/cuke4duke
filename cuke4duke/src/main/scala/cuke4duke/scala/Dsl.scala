package cuke4duke.scala

import _root_.scala.collection.mutable.ListBuffer
import _root_.scala.reflect.Manifest
import cuke4duke.internal.JRuby
import cuke4duke.internal.language.AbstractProgrammingLanguage
import collection.mutable.Map
import cuke4duke.internal.scala._
import cuke4duke.{StepMother, Table}
/*
  <yourclass> {extends|with} Dsl with EN
 */
trait Dsl {

  private [cuke4duke] val stepDefinitions = new ListBuffer[(AbstractProgrammingLanguage,ScalaTransformations) => ScalaStepDefinition]
  private [cuke4duke] val beforeHooks = new ListBuffer[ScalaHook]
  private [cuke4duke] val afterHooks = new ListBuffer[ScalaHook]
  private [cuke4duke] val transformations = Map[Class[_], String => Option[_]]()

  def Before(f: => Unit) = beforeHooks += new ScalaHook(Nil.toArray, f _)
  def Before(tags: String*)(f: => Unit) = beforeHooks += new ScalaHook(tags.toArray, f _)

  def After(f: => Unit) = afterHooks += new ScalaHook(Nil.toArray, f _)
  def After(tags: String*)(f: => Unit) = afterHooks += new ScalaHook(tags.toArray, f _)

  def pending(message:String){ throw JRuby.cucumberPending(message) }
  def pending{ pending("TODO") }

  def Transform[T](f:String => Option[T])(implicit m:Manifest[T]){
    transformations(m.erasure) = f
  }

  sealed trait CreateHandle{
    def apply(name:String, regex:String):Handle
    def apply(name:String, regex:String, table:Table):Unit
    def apply(name:String, regex:String, py:String):Unit
  }

  private var handleRegex:CreateHandle = new CreateHandle {
    override def apply(name:String, regex:String) = new Handle{
      def apply(fun:Fun) = stepDefinitions += ((programmingLanguage:AbstractProgrammingLanguage, t:ScalaTransformations) => new ScalaStepDefinition(name, regex, fun.f, fun.types, t, programmingLanguage))
    }
    override def apply(name:String, regex:String, table:Table) = error(name+"("+regex+", 'Table') is only inteded for calling other steps")
    override def apply(name:String, regex:String, py:String) = error(name+"("+regex+", 'String') is only inteded for calling other steps")
  }

  private [cuke4duke] def executionMode(stepMother:StepMother){
    handleRegex = new CreateHandle {
      override def apply(name:String, regex:String) = new Handle{
        stepMother.invoke(regex)
        def apply(fun:Fun) = error("cannot register new stepdefinitions in execution mode")
      }
      override def apply(name:String, regex:String, table:Table) = stepMother.invoke(regex, table)
      override def apply(name:String, regex:String, py:String) = stepMother.invoke(regex, py)
    }
  }

  sealed trait Handle {
    //treat call-by-name like a Fun of Function0
    def apply(f: => Unit):Unit = apply(f0toFun(f _))
    def apply(fun:Fun)
  }

  final class Step(name:String) {
    def apply(regex:String):Handle = handleRegex(name, regex)
    def apply(regex:String, py:String):Unit = handleRegex(name, regex, py)
    def apply(regex:String, table:Table):Unit = handleRegex(name, regex, table)
  }

  final class Fun private[Dsl](private [Dsl] val f: Any, manifests: Manifest[_]*) {
    private [Dsl] val types = manifests.toList.map(_.erasure)
  }

  // treat Handle like a Fun of Function0
  implicit def handle2Fun(h:Handle) = new Fun(() => h)
  // only functions can be converted to 'Fun' instances
  implicit def f0toFun(f: Function0[_]) = new Fun(f)
  implicit def f1toFun[T1, _](f: Function1[T1, _])(implicit m1: Manifest[T1]) = new Fun(f, m1)
  implicit def f2toFun[T1, T2, _](f: Function2[T1, T2, _])(implicit m1: Manifest[T1], m2: Manifest[T2]) = new Fun(f, m1, m2)
  implicit def f3toFun[T1, T2, T3, _](f: Function3[T1, T2, T3, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3]) = new Fun(f, m1, m2, m3)
  implicit def f4toFun[T1, T2, T3, T4, _](f: Function4[T1, T2, T3, T4, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4]) = new Fun(f, m1, m2, m3, m4)
  implicit def f5toFun[T1, T2, T3, T4, T5, _](f: Function5[T1, T2, T3, T4, T5, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5]) = new Fun(f, m1, m2, m3, m4, m5)
  implicit def f6toFun[T1, T2, T3, T4, T5, T6, _](f: Function6[T1, T2, T3, T4, T5, T6, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6]) = new Fun(f, m1, m2, m3, m4, m5, m6)
  implicit def f7toFun[T1, T2, T3, T4, T5, T6, T7, _](f: Function7[T1, T2, T3, T4, T5, T6, T7, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7)
  implicit def f8toFun[T1, T2, T3, T4, T5, T6, T7, T8, _](f: Function8[T1, T2, T3, T4, T5, T6, T7, T8, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8)
  implicit def f9toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, _](f: Function9[T1, T2, T3, T4, T5, T6, T7, T8, T9, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9)
  implicit def f10toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, _](f: Function10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10)
  implicit def f11toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, _](f: Function11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11)
  implicit def f12toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, _](f: Function12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12)
  implicit def f13toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, _](f: Function13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13)
  implicit def f14toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, _](f: Function14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14)
  implicit def f15toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, _](f: Function15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15)
  implicit def f16toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, _](f: Function16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16)
  implicit def f17toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, _](f: Function17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17)
  implicit def f18toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, _](f: Function18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18)
  implicit def f19toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, _](f: Function19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19)
  implicit def f20toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, _](f: Function20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19], m20: Manifest[T20]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20)
  implicit def f21toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, _](f: Function21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19], m20: Manifest[T20], m21: Manifest[T21]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21)
  implicit def f22toFun[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, _](f: Function22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, _])(implicit m1: Manifest[T1], m2: Manifest[T2], m3: Manifest[T3], m4: Manifest[T4], m5: Manifest[T5], m6: Manifest[T6], m7: Manifest[T7], m8: Manifest[T8], m9: Manifest[T9], m10: Manifest[T10], m11: Manifest[T11], m12: Manifest[T12], m13: Manifest[T13], m14: Manifest[T14], m15: Manifest[T15], m16: Manifest[T16], m17: Manifest[T17], m18: Manifest[T18], m19: Manifest[T19], m20: Manifest[T20], m21: Manifest[T21], m22: Manifest[T22]) = new Fun(f, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16, m17, m18, m19, m20, m21, m22)
}