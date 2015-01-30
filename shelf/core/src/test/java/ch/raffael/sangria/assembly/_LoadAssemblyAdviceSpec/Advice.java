package ch.raffael.sangria.assembly._LoadAssemblyAdviceSpec;

import ch.raffael.sangria.annotations.DependsOn;
import ch.raffael.sangria.annotations.Install;
import ch.raffael.sangria.annotations.Using;
import ch.raffael.sangria.assembly._LoadAssemblyAdviceSpec.cap.NotLoadableModule;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Install
@DependsOn(NotLoadableModule.class)
@Using("use.this")
public class Advice {

}
