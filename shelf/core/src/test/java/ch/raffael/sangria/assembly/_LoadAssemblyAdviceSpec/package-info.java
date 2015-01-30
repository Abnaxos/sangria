/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@Feature
@Extends("extends.this")
@Extends("extends.that")
@Provides("provide.this")
@LinkTo("linked.pkg, super")
@OutboundLinks("external.pkg")
@OutboundLinks("more.(pack, ages)")
@OutboundLinks("this.rel")
@Using("use.this")
package ch.raffael.sangria.assembly._LoadAssemblyAdviceSpec;

import ch.raffael.sangria.annotations.Feature;
import ch.raffael.sangria.annotations.Extends;
import ch.raffael.sangria.annotations.OutboundLinks;
import ch.raffael.sangria.annotations.LinkTo;
import ch.raffael.sangria.annotations.Provides;
import ch.raffael.sangria.annotations.Using;
