/**
 * Deployment
 * ==========
 *
 * A sangria application gets deployed in three phases:
 *
 * 1.  **Boostrap**
 *
 *     In the bootstrap phase, Sangria will create a Injector that primarily does two
 *     things:
 *
 *      *  scan for bundles
 *      *  establish the environment
 *
 *     *{@link ch.raffael.sangria.bootstrap_old (More information)}*
 *
 * 2.  **Assembly**
 *
 *     The assembly phase doesn't use a Injector and is therefore not extensible. In
 *     this phase the features and ClassLoader delegation are resolved.
 *
 *     *{@link ch.raffael.sangria.assembly (More information)}*
 *
 * 3.  **Configuration**
 *
 * 4.  **Runtime**
 *
 *
 *
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
package ch.raffael.sangria.deploy;
