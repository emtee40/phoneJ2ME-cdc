/*
 * 
 * @(#)ResourceBundle.java	1.74 06/10/10
 * 
 * Portions Copyright  2000-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1999 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import sun.misc.SoftCache;

/**
 *
 * Resource bundles contain locale-specific objects.
 * When your program needs a locale-specific resource,
 * a <code>String</code> for example, your program can load it
 * from the resource bundle that is appropriate for the
 * current user's locale. In this way, you can write
 * program code that is largely independent of the user's
 * locale isolating most, if not all, of the locale-specific
 * information in resource bundles.
 *
 * <p>
 * This allows you to write programs that can:
 * <UL type=SQUARE>
 * <LI> be easily localized, or translated, into different languages
 * <LI> handle multiple locales at once
 * <LI> be easily modified later to support even more locales
 * </UL>
 *
 * <P>
 * Resource bundles belong to families whose members share a common base 
 * name, but whose names also have additional components that identify 
 * their locales. For example, the base name of a family of resource 
 * bundles might be "MyResources". The family should have a default 
 * resource bundle which simply has the same name as its family - 
 * "MyResources" - and will be used as the bundle of last resort if a
 * specific locale is not supported. The family can then provide as
 * many locale-specific members as needed, for example a German one
 * named "MyResources_de".
 *
 * <P>
 * Each resource bundle in a family contains the same items, but the items have
 * been translated for the locale represented by that resource bundle.
 * For example, both "MyResources" and "MyResources_de" may have a
 * <code>String</code> that's used on a button for canceling operations.
 * In "MyResources" the <code>String</code> may contain "Cancel" and in
 * "MyResources_de" it may contain "Abbrechen".
 *
 * <P>
 * If there are different resources for different countries, you
 * can make specializations: for example, "MyResources_de_CH" contains objects for
 * the German language (de) in Switzerland (CH). If you want to only
 * modify some of the resources
 * in the specialization, you can do so.
 *
 * <P>
 * When your program needs a locale-specific object, it loads
 * the <code>ResourceBundle</code> class using the
 * {@link #getBundle(java.lang.String, java.util.Locale) getBundle}
 * method:
 * <blockquote>
 * <pre>
 * ResourceBundle myResources =
 *      ResourceBundle.getBundle("MyResources", currentLocale);
 * </pre>
 * </blockquote>
 *
 * <P>
 * Resource bundles contain key/value pairs. The keys uniquely
 * identify a locale-specific object in the bundle. Here's an
 * example of a <code>ListResourceBundle</code> that contains
 * two key/value pairs:
 * <blockquote>
 * <pre>
 * public class MyResources extends ListResourceBundle {
 *      public Object[][] getContents() {
 *              return contents;
 *      }
 *      static final Object[][] contents = {
 *      // LOCALIZE THIS
 *              {"OkKey", "OK"},
 *              {"CancelKey", "Cancel"},
 *      // END OF MATERIAL TO LOCALIZE
 *      };
 * }
 * </pre>
 * </blockquote>
 * Keys are always <code>String</code>s.
 * In this example, the keys are "OkKey" and "CancelKey".
 * In the above example, the values
 * are also <code>String</code>s--"OK" and "Cancel"--but
 * they don't have to be. The values can be any type of object.
 *
 * <P>
 * You retrieve an object from resource bundle using the appropriate
 * getter method. Because "OkKey" and "CancelKey"
 * are both strings, you would use <code>getString</code> to retrieve them:
 * <blockquote>
 * <pre>
 * button1 = new Button(myResources.getString("OkKey"));
 * button2 = new Button(myResources.getString("CancelKey"));
 * </pre>
 * </blockquote>
 * The getter methods all require the key as an argument and return
 * the object if found. If the object is not found, the getter method
 * throws a <code>MissingResourceException</code>.
 *
 * <P>
 * Besides <code>getString</code>, ResourceBundle also provides
 * a method for getting string arrays, <code>getStringArray</code>,
 * as well as a generic <code>getObject</code> method for any other
 * type of object. When using <code>getObject</code>, you'll
 * have to cast the result to the appropriate type. For example:
 * <blockquote>
 * <pre>
 * int[] myIntegers = (int[]) myResources.getObject("intList");
 * </pre>
 * </blockquote>
 *
 * <P>
 * The Java 2 platform provides two subclasses of <code>ResourceBundle</code>,
 * <code>ListResourceBundle</code> and <code>PropertyResourceBundle</code>,
 * that provide a fairly simple way to create resources.
 * As you saw briefly in a previous example, <code>ListResourceBundle</code>
 * manages its resource as a List of key/value pairs.
 * <code>PropertyResourceBundle</code> uses a properties file to manage
 * its resources.
 *
 * <p>
 * If <code>ListResourceBundle</code> or <code>PropertyResourceBundle</code>
 * do not suit your needs, you can write your own <code>ResourceBundle</code>
 * subclass.  Your subclasses must override two methods: <code>handleGetObject</code>
 * and <code>getKeys()</code>.
 *
 * <P>
 * The following is a very simple example of a <code>ResourceBundle</code>
 * subclass, MyResources, that manages two resources (for a larger number of
 * resources you would probably use a <code>Hashtable</code>).
 * Notice that you don't need to supply a value if 
 * a "parent-level" <code>ResourceBundle</code> handles the same
 * key with the same value (as for the okKey below).
 * <p><strong>Example:</strong>
 * <blockquote>
 * <pre>
 * // default (English language, United States)
 * public class MyResources extends ResourceBundle {
 *     public Object handleGetObject(String key) {
 *         if (key.equals("okKey")) return "Ok";
 *         if (key.equals("cancelKey")) return "Cancel";
 *         return null;
 *     }
 * }
 *
 * // German language
 * public class MyResources_de extends MyResources {
 *     public Object handleGetObject(String key) {
 *         // don't need okKey, since parent level handles it.
 *         if (key.equals("cancelKey")) return "Abbrechen";
 *         return null;
 *     }
 * }
 * </pre>
 * </blockquote>
 * You do not have to restrict yourself to using a single family of
 * <code>ResourceBundle</code>s. For example, you could have a set of bundles for
 * exception messages, <code>ExceptionResources</code>
 * (<code>ExceptionResources_fr</code>, <code>ExceptionResources_de</code>, ...),
 * and one for widgets, <code>WidgetResource</code> (<code>WidgetResources_fr</code>,
 * <code>WidgetResources_de</code>, ...); breaking up the resources however you like.
 *
 * @see ListResourceBundle
 * @see PropertyResourceBundle
 * @see MissingResourceException
 * @since JDK1.1
 */
abstract public class ResourceBundle {
    /**
     * Static key used for resource lookups. Concurrent
     * access to this object is controlled by synchronizing cacheList,
     * not cacheKey.  A static object is used to do cache lookups
     * for performance reasons - the assumption being that synchronization
     * has a lower overhead than object allocation and subsequent
     * garbage collection.
     */
    private static final ResourceCacheKey cacheKey = new ResourceCacheKey();

    /** initial size of the bundle cache */
    private static final int INITIAL_CACHE_SIZE = 25;

    /** capacity of cache consumed before it should grow */
    private static final float CACHE_LOAD_FACTOR = (float)1.0;

    /**
     * Maximum length of one branch of the resource search path tree.
     * Used in getBundle.
     */
    private static final int MAX_BUNDLES_SEARCHED = 3;

    /**
     * This Hashtable is used to keep multiple threads from loading the
     * same bundle concurrently.  The table entries are (cacheKey, thread)
     * where cacheKey is the key for the bundle that is under construction
     * and thread is the thread that is constructing the bundle.
     * This list is manipulated in findBundle and putBundleInCache.
     * Synchronization of this object is done through cacheList, not on
     * this object.
     */
    private static final Hashtable underConstruction = new Hashtable(MAX_BUNDLES_SEARCHED, CACHE_LOAD_FACTOR);

    /** constant indicating that no resource bundle was found */
    private static final Object NOT_FOUND = new Object();

    /**
     * The cache is a map from cache keys (with bundle base name,
     * locale, and class loader) to either a resource bundle
     * (if one has been found) or NOT_FOUND (if no bundle has
     * been found).
     * The cache is a SoftCache, allowing bundles to be
     * removed from the cache if they are no longer
     * needed.  This will also allow the cache keys
     * to be reclaimed along with the ClassLoaders
     * they reference.
     * This variable would be better named "cache", but we keep the old
     * name for compatibility with some workarounds for bug 4212439.
     */
    private static SoftCache cacheList = new SoftCache(INITIAL_CACHE_SIZE, CACHE_LOAD_FACTOR);

    /**
     * Queue for reference objects referring to class loaders.
     */
    private static ReferenceQueue referenceQueue = new ReferenceQueue();

    /**
     * The parent bundle of this bundle.
     * The parent bundle is searched by {@link #getObject getObject}
     * when this bundle does not contain a particular resource.
     */
    protected ResourceBundle parent = null;

    /**
     * The locale for this bundle.
     */
    private Locale locale = null;

    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    public ResourceBundle() {
    }

    /**
     * Gets a string for the given key from this resource bundle or one of its parents.
     * Calling this method is equivalent to calling
     * <blockquote>
     * <code>(String) {@link #getObject(java.lang.String) getObject}(key)</code>.
     * </blockquote>
     *
     * @param key the key for the desired string
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no object for the given key can be found
     * @exception ClassCastException if the object found for the given key is not a string
     * @return the string for the given key
     */
    public final String getString(String key) {
        return (String) getObject(key);
    }

    /**
     * Gets a string array for the given key from this resource bundle or one of its parents.
     * Calling this method is equivalent to calling
     * <blockquote>
     * <code>(String[]) {@link #getObject(java.lang.String) getObject}(key)</code>.
     * </blockquote>
     *
     * @param key the key for the desired string array
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no object for the given key can be found
     * @exception ClassCastException if the object found for the given key is not a string array
     * @return the string array for the given key
     */
    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }

    /**
     * Gets an object for the given key from this resource bundle or one of its parents.
     * This method first tries to obtain the object from this resource bundle using
     * {@link #handleGetObject(java.lang.String) handleGetObject}.
     * If not successful, and the parent resource bundle is not null,
     * it calls the parent's <code>getObject</code> method.
     * If still not successful, it throws a MissingResourceException.
     *
     * @param key the key for the desired object
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no object for the given key can be found
     * @return the object for the given key
     */
    public final Object getObject(String key) {
        Object obj = handleGetObject(key);
        if (obj == null) {
            if (parent != null) {
                obj = parent.getObject(key);
            }
            if (obj == null)
                throw new MissingResourceException("Can't find resource for bundle "
                                                   +this.getClass().getName()
                                                   +", key "+key,
                                                   this.getClass().getName(),
                                                   key);
        }
        return obj;
    }

    /**
     * Returns the locale of this resource bundle. This method can be used after a
     * call to getBundle() to determine whether the resource bundle returned really
     * corresponds to the requested locale or is a fallback.
     *
     * @return the locale of this resource bundle
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale for this bundle.  This is the locale that this
     * bundle actually represents and does not depend on how the
     * bundle was found by getBundle.  Ex. if the user was looking
     * for fr_FR and getBundle found en_US, the bundle's locale would
     * be en_US, NOT fr_FR
     * @param baseName the bundle's base name
     * @param bundleName the complete bundle name including locale
     * extension.
     */
    private void setLocale(String baseName, String bundleName) {
        if (baseName.length() == bundleName.length()) {
            locale = new Locale("", "");
        } else if (baseName.length() < bundleName.length()) {
            int pos = baseName.length();
            String temp = bundleName.substring(pos + 1);
            pos = temp.indexOf('_');
            if (pos == -1) {
                locale = new Locale(temp, "", "");
                return;
            }

            String language = temp.substring(0, pos);
            temp = temp.substring(pos + 1);
            pos = temp.indexOf('_');
            if (pos == -1) {
                locale = new Locale(language, temp, "");
                return;
            }

            String country = temp.substring(0, pos);
            temp = temp.substring(pos + 1);

            locale = new Locale(language, country, temp);
        } else {
            //The base name is longer than the bundle name.  Something is very wrong
            //with the calling code.
            throw new IllegalArgumentException();
        }
    }

    /*
     * Automatic determination of the ClassLoader to be used to load
     * resources on behalf of the client.  N.B. The client is getLoader's
     * caller's caller.
     */
    private static ClassLoader getLoader() {
        Class[] stack = getClassContext();
        /* Magic number 2 identifies our caller's caller */
        Class c = stack[2];
        ClassLoader cl = (c == null) ? null : c.getClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    private static native Class[] getClassContext();

    /**
     * Sets the parent bundle of this bundle.
     * The parent bundle is searched by {@link #getObject getObject}
     * when this bundle does not contain a particular resource.
     *
     * @param parent this bundle's parent bundle.
     */
    protected void setParent( ResourceBundle parent ) {
        this.parent = parent;
    }

     /**
      * Key used for cached resource bundles.  The key checks
      * the resource name, the class loader, and the default
      * locale to determine if the resource is a match to the
      * requested one. The loader may be null, but the
      * searchName and the default locale must have a non-null value.
      * Note that the default locale may change over time, and
      * lookup should always be based on the current default
      * locale (if at all).
      */
    private static final class ResourceCacheKey implements Cloneable {
        private LoaderReference loaderRef;
        private String searchName;
        private Locale defaultLocale;
        private int hashCodeCache;

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            try {
                final ResourceCacheKey otherEntry = (ResourceCacheKey)other;
                //quick check to see if they are not equal
                if (hashCodeCache != otherEntry.hashCodeCache) {
                    return false;
                }
                //are the names the same?
                if (!searchName.equals(otherEntry.searchName)) {
                    return false;
                }
                // are the default locales the same?
                if (defaultLocale == null) {
                    if (otherEntry.defaultLocale != null) {
                        return false;
                    }
                } else {
                    if (!defaultLocale.equals(otherEntry.defaultLocale)) {
                        return false;
                    }
                }
                //are refs (both non-null) or (both null)?
                if (loaderRef == null) {
                    return otherEntry.loaderRef == null;
                } else {
                    Object loaderRefValue = loaderRef.get();
                    return (otherEntry.loaderRef != null)
                            // with a null reference we can no longer find
                            // out which class loader was referenced; so
                            // treat it as unequal
                            && (loaderRefValue != null)
                            && (loaderRefValue == otherEntry.loaderRef.get());
                }
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e) {
                return false;
            }
        }

        public int hashCode() {
            return hashCodeCache;
        }

        public Object clone() {
            try {
                ResourceCacheKey clone = (ResourceCacheKey) super.clone();
                if (loaderRef != null) {
                    clone.loaderRef = new LoaderReference(loaderRef.get(), referenceQueue, clone);
                }
                return clone;
            } catch (CloneNotSupportedException e) {
                //this should never happen
                throw new InternalError();
            }

        }

        public void setKeyValues(ClassLoader loader, String searchName, Locale defaultLocale) {
            this.searchName = searchName;
            hashCodeCache = searchName.hashCode();
            this.defaultLocale = defaultLocale;
            if (defaultLocale != null) {
                hashCodeCache ^= defaultLocale.hashCode();
            }
            if (loader == null) {
                this.loaderRef = null;
            } else {
                loaderRef = new LoaderReference(loader, referenceQueue, this);
                hashCodeCache ^= loader.hashCode();
            }
        }

        public void clear() {
            setKeyValues(null, "", null);
        }
    }

    /**
     * References to class loaders are weak references, so that they can be
     * garbage collected when nobody else is using them. The ResourceBundle
     * class has no reason to keep class loaders alive.
     */
    private static final class LoaderReference extends WeakReference {
        private ResourceCacheKey cacheKey;

        LoaderReference(Object referent, ReferenceQueue q, ResourceCacheKey key) {
            super(referent, q);
            cacheKey = key;
        }

        ResourceCacheKey getCacheKey() {
            return cacheKey;
        }
    }


    /**
     * Gets a resource bundle using the specified base name, the default locale,
     * and the caller's class loader. Calling this method is equivalent to calling
     * <blockquote>
     * <code>getBundle(baseName, Locale.getDefault(), this.getClass().getClassLoader())</code>,
     * </blockquote>
     * except that <code>getClassLoader()</code> is run with the security
     * privileges of <code>ResourceBundle</code>.
     * See {@link #getBundle(java.lang.String, java.util.Locale, java.lang.ClassLoader) getBundle}
     * for a complete description of the search and instantiation strategy.
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @exception java.lang.NullPointerException
     *     if <code>baseName</code> is <code>null</code>
     * @exception MissingResourceException
     *     if no resource bundle for the specified base name can be found
     * @return a resource bundle for the given base name and the default locale
     */
    public static final ResourceBundle getBundle(String baseName)
    {
        return getBundleImpl(baseName, Locale.getDefault(),
        /* must determine loader here, else we break stack invariant */
        getLoader());
    }

    /**
     * Gets a resource bundle using the specified base name and locale,
     * and the caller's class loader. Calling this method is equivalent to calling
     * <blockquote>
     * <code>getBundle(baseName, locale, this.getClass().getClassLoader())</code>,
     * </blockquote>
     * except that <code>getClassLoader()</code> is run with the security
     * privileges of <code>ResourceBundle</code>.
     * See {@link #getBundle(java.lang.String, java.util.Locale, java.lang.ClassLoader) getBundle}
     * for a complete description of the search and instantiation strategy.
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param locale the locale for which a resource bundle is desired
     * @exception java.lang.NullPointerException
     *     if <code>baseName</code> or <code>locale</code> is <code>null</code>
     * @exception MissingResourceException
     *     if no resource bundle for the specified base name can be found
     * @return a resource bundle for the given base name and locale
     */
    public static final ResourceBundle getBundle(String baseName,
                                                         Locale locale)
    {
        return getBundleImpl(baseName, locale, getLoader());
    }

    /**
     * Gets a resource bundle using the specified base name, locale, and class loader.
     *
     * <p>
     * Conceptually, <code>getBundle</code> uses the following strategy for locating and instantiating
     * resource bundles:
     * <p>
     * <code>getBundle</code> uses the base name, the specified locale, and the default
     * locale (obtained from {@link java.util.Locale#getDefault() Locale.getDefault})
     * to generate a sequence of <em>candidate bundle names</em>.
     * If the specified locale's language, country, and variant are all empty
     * strings, then the base name is the only candidate bundle name.
     * Otherwise, the following sequence is generated from the attribute
     * values of the specified locale (language1, country1, and variant1)
     * and of the default locale (language2, country2, and variant2):
     * <ul>
     * <li> baseName + "_" + language1 + "_" + country1 + "_" + variant1
     * <li> baseName + "_" + language1 + "_" + country1
     * <li> baseName + "_" + language1
     * <li> baseName + "_" + language2 + "_" + country2 + "_" + variant2
     * <li> baseName + "_" + language2 + "_" + country2
     * <li> baseName + "_" + language2
     * <li> baseName
     * </ul>
     * <p>
     * Candidate bundle names where the final component is an empty string are omitted.
     * For example, if country1 is an empty string, the second candidate bundle name is omitted.
     *
     * <p>
     * <code>getBundle</code> then iterates over the candidate bundle names to find the first
     * one for which it can <em>instantiate</em> an actual resource bundle. For each candidate
     * bundle name, it attempts to create a resource bundle:
     * <ul>
     * <li>
     * First, it attempts to load a class using the candidate bundle name.
     * If such a class can be found and loaded using the specified class loader, is assignment
     * compatible with ResourceBundle, is accessible from ResourceBundle, and can be instantiated,
     * <code>getBundle</code> creates a new instance of this class and uses it as the <em>result
     * resource bundle</em>.
     * <li>
     * Otherwise, <code>getBundle</code> attempts to locate a property resource file.
     * It generates a path name from the candidate bundle name by replacing all "." characters
     * with "/" and appending the string ".properties".
     * It attempts to find a "resource" with this name using
     * {@link java.lang.ClassLoader#getResource(java.lang.String) ClassLoader.getResource}.
     * (Note that a "resource" in the sense of <code>getResource</code> has nothing to do with
     * the contents of a resource bundle, it is just a container of data, such as a file.)
     * If it finds a "resource", it attempts to create a new
     * {@link PropertyResourceBundle} instance from its contents.
     * If successful, this instance becomes the <em>result resource bundle</em>.
     * </ul>
     *
     * <p>
     * If no result resource bundle has been found, a <code>MissingResourceException</code>
     * is thrown.
     *
     * <p>
     * Once a result resource bundle has been found, its parent chain is instantiated.
     * <code>getBundle</code> iterates over the candidate bundle names that can be
     * obtained by successively removing variant, country, and language
     * (each time with the preceding "_") from the bundle name of the result resource bundle.
     * As above, candidate bundle names where the final component is an empty string are omitted.
     * With each of the candidate bundle names it attempts to instantiate a resource bundle, as
     * described above.
     * Whenever it succeeds, it calls the previously instantiated resource
     * bundle's {@link #setParent(java.util.ResourceBundle) setParent} method
     * with the new resource bundle, unless the previously instantiated resource
     * bundle already has a non-null parent.
     *
     * <p>
     * Implementations of <code>getBundle</code> may cache instantiated resource bundles
     * and return the same resource bundle instance multiple times. They may also
     * vary the sequence in which resource bundles are instantiated as long as the
     * selection of the result resource bundle and its parent chain are compatible with
     * the description above.
     *
     * <p>
     * The <code>baseName</code> argument should be a fully qualified class name. However, for
     * compatibility with earlier versions, Sun's Java 2 runtime environments do not verify this,
     * and so it is possible to access <code>PropertyResourceBundle</code>s by specifying a
     * path name (using "/") instead of a fully qualified class name (using ".").
     *
     * <p>
     * <strong>Example:</strong> The following class and property files are provided:
     * MyResources.class, MyResources_fr_CH.properties, MyResources_fr_CH.class,
     * MyResources_fr.properties, MyResources_en.properties, MyResources_es_ES.class.
     * The contents of all files are valid (that is, public non-abstract subclasses of ResourceBundle for
     * the ".class" files, syntactically correct ".properties" files).
     * The default locale is <code>Locale("en", "GB")</code>.
     * <p>
     * Calling <code>getBundle</code> with the shown locale argument values instantiates
     * resource bundles from the following sources:
     * <ul>
     * <li>Locale("fr", "CH"): result MyResources_fr_CH.class, parent MyResources_fr.properties, parent MyResources.class
     * <li>Locale("fr", "FR"): result MyResources_fr.properties, parent MyResources.class
     * <li>Locale("de", "DE"): result MyResources_en.properties, parent MyResources.class
     * <li>Locale("en", "US"): result MyResources_en.properties, parent MyResources.class
     * <li>Locale("es", "ES"): result MyResources_es_ES.class, parent MyResources.class
     * </ul>
     * The file MyResources_fr_CH.properties is never used because it is hidden by
     * MyResources_fr_CH.class.
     *
     * <p>
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param locale the locale for which a resource bundle is desired
     * @param loader the class loader from which to load the resource bundle
     * @exception java.lang.NullPointerException
     *     if <code>baseName</code>, <code>locale</code>, or <code>loader</code> is <code>null</code>
     * @exception MissingResourceException
     *     if no resource bundle for the specified base name can be found
     * @return a resource bundle for the given base name and locale
     * @since 1.2
     */
    public static ResourceBundle getBundle(String baseName, Locale locale,
                                           ClassLoader loader)
    {
        if (loader == null) {
            throw new NullPointerException();
        }
        return getBundleImpl(baseName, locale, loader);
    }

    private static ResourceBundle getBundleImpl(String baseName, Locale locale,
                                           ClassLoader loader)
    {
        if (baseName == null) {
            throw new NullPointerException();
        }

        //fast path the case where the bundle is cached
        String bundleName = baseName;
        String localeSuffix = locale.toString();
        if (localeSuffix.length() > 0) {
            bundleName += "_" + localeSuffix;
        } else if (locale.getVariant().length() > 0) {
            //This corrects some strange behavior in Locale where
            //new Locale("", "", "VARIANT").toString == ""
            bundleName += "___" + locale.getVariant();
        }
        
        // The default locale may influence the lookup result, and
        // it may change, so we get it here once.
        Locale defaultLocale = Locale.getDefault();

        Object lookup = findBundleInCache(loader, bundleName, defaultLocale);
        if (lookup == NOT_FOUND) {
            throwMissingResourceException(baseName, locale);
        } else if (lookup != null) {
            return (ResourceBundle)lookup;
        }

        //The bundle was not cached, so start doing lookup at the root
        //Resources are loaded starting at the root and working toward
        //the requested bundle.

        //If findBundle returns null, we become responsible for defining
        //the bundle, and must call putBundleInCache to complete this
        //task.  This is critical because other threads may be waiting
        //for us to finish.

        Object parent = NOT_FOUND;
        try {
            //locate the root bundle and work toward the desired child
            Object root = findBundle(loader, baseName, defaultLocale, baseName, null);
            if (root == null) {
                putBundleInCache(loader, baseName, defaultLocale, NOT_FOUND);
                root = NOT_FOUND;
            }

            // Search the main branch of the search tree.
            // We need to keep references to the bundles we find on the main path
            // so they don't get garbage collected before we get to propagate().
            final Vector names = calculateBundleNames(baseName, locale);
            Vector bundlesFound = new Vector(MAX_BUNDLES_SEARCHED);
	    // if we found the root bundle and no other bundle names are needed
	    // we can stop here. We don't need to search or load anything further.
            boolean foundInMainBranch = (root != NOT_FOUND && names.size() == 0);
	    
	    if (!foundInMainBranch) {
	      parent = root;
	      for (int i = 0; i < names.size(); i++) {
                bundleName = (String)names.elementAt(i);
                lookup = findBundle(loader, bundleName, defaultLocale, baseName, parent);
                bundlesFound.addElement(lookup);
                if (lookup != null) {
                    parent = lookup;
                    foundInMainBranch = true;
                }
	      }
            }
            parent = root;
            if (!foundInMainBranch) {
                //we didn't find anything on the main branch, so we do the fallback branch
                final Vector fallbackNames = calculateBundleNames(baseName, defaultLocale);
                for (int i = 0; i < fallbackNames.size(); i++) {
                    bundleName = (String)fallbackNames.elementAt(i);
                    if (names.contains(bundleName)) {
                        //the fallback branch intersects the main branch so we can stop now.
                        break;
                    }
                    lookup = findBundle(loader, bundleName, defaultLocale, baseName, parent);
                    if (lookup != null) {
                        parent = lookup;
                    } else {
                        //propagate the parent to the child.  We can do this
                        //here because we are in the default path.
                        putBundleInCache(loader, bundleName, defaultLocale, parent);
                    }
                }
            }
            //propagate the inheritance/fallback down through the main branch
            parent = propagate(loader, names, bundlesFound, defaultLocale, parent);
        } catch (Exception e) {
            //We should never get here unless there has been a change
            //to the code that doesn't catch it's own exceptions.
            cleanUpConstructionList();
            throwMissingResourceException(baseName, locale);
        } catch (Error e) {
            //The only Error that can currently hit this code is a ThreadDeathError
            //but errors might be added in the future, so we'll play it safe and
            //clean up.
            cleanUpConstructionList();
            throw e;
        }
        if (parent == NOT_FOUND) {
            throwMissingResourceException(baseName, locale);
        }
        return (ResourceBundle)parent;
    }

    /**
     * propagate bundles from the root down the specified branch of the search tree.
     * @param loader the class loader for the bundles
     * @param names the names of the bundles along search path
     * @param bundlesFound the bundles corresponding to the names (some may be null)
     * @param defaultLocale the default locale at the time getBundle was called
     * @param parent the parent of the first bundle in the path (the root bundle)
     * @return the value of the last bundle along the path
     */
    private static Object propagate(ClassLoader loader, Vector names,
            Vector bundlesFound, Locale defaultLocale, Object parent) {
        for (int i = 0; i < names.size(); i++) {
            final String bundleName = (String)names.elementAt(i);
            final Object lookup = bundlesFound.elementAt(i);
            if (lookup == null) {
                putBundleInCache(loader, bundleName, defaultLocale, parent);
            } else {
                parent = lookup;
            }
        }
        return parent;
    }

    /** Throw a MissingResourceException with proper message */
    private static void throwMissingResourceException(String baseName, Locale locale)
            throws MissingResourceException{
        throw new MissingResourceException("Can't find bundle for base name "
                                           + baseName + ", locale " + locale,
                                           baseName + "_" + locale,"");
    }

    /**
     * Remove any entries this thread may have in the construction list.
     * This is done as cleanup in the case where a bundle can't be
     * constructed.
     */
    private static void cleanUpConstructionList() {
        synchronized (cacheList) {
            final Collection entries = underConstruction.values();
            final Thread thisThread = Thread.currentThread();
            while (entries.remove(thisThread)) {
            }
        }
    }

    /**
     * Find a bundle in the cache or load it via the loader or a property file.
     * If the bundle isn't found, an entry is put in the constructionCache
     * and null is returned.  If null is returned, the caller must define the bundle
     * by calling putBundleInCache.  This routine also propagates NOT_FOUND values
     * from parent to child bundles when the parent is NOT_FOUND.
     * @param loader the loader to use when loading a bundle
     * @param bundleName the complete bundle name including locale extension
     * @param defaultLocale the default locale at the time getBundle was called
     * @param parent the parent of the resource bundle being loaded.  null if
     * the bundle is a root bundle
     * @return the bundle or null if the bundle could not be found in the cache
     * or loaded.
     */
    private static Object findBundle(ClassLoader loader, String bundleName, Locale defaultLocale,
            String baseName, Object parent) {
        Object result;
        synchronized (cacheList) {
            // Before we do the real work of this method, see
            // whether we need to do some housekeeping:
            // If references to class loaders have been nulled out,
            // remove all related information from the cache
            Reference ref = referenceQueue.poll();
            while (ref != null) {
                cacheList.remove(((LoaderReference) ref).getCacheKey());
                ref = referenceQueue.poll();
            }

            //check for bundle in cache
            cacheKey.setKeyValues(loader, bundleName, defaultLocale);
            result = cacheList.get(cacheKey);
            if (result != null) {
                cacheKey.clear();
                return result;
            }
            // check to see if some other thread is building this bundle.
            // Note that there is a rare chance that this thread is already
            // working on this bundle, and in the process getBundle was called
            // again, in which case we can't wait (4300693)
            Thread builder = (Thread) underConstruction.get(cacheKey);
            boolean beingBuilt = (builder != null && builder != Thread.currentThread());
            //if some other thread is building the bundle...
            if (beingBuilt) {
                //while some other thread is building the bundle...
                while (beingBuilt) {
                    cacheKey.clear();
                    try {
                        //Wait until the bundle is complete
                        cacheList.wait();
                    } catch (InterruptedException e) {
                    }
                    cacheKey.setKeyValues(loader, bundleName, defaultLocale);
                    beingBuilt = underConstruction.containsKey(cacheKey);
                }
                //if someone constructed the bundle for us, return it
                result = cacheList.get(cacheKey);
                if (result != null) {
                    cacheKey.clear();
                    return result;
                }
            }
            //The bundle isn't in the cache, so we are now responsible for
            //loading it and adding it to the cache.
            final Object key = cacheKey.clone();
            underConstruction.put(key, Thread.currentThread());
            //the bundle is removed from the cache by putBundleInCache
            cacheKey.clear();
        }

        //try loading the bundle via the class loader
        result = loadBundle(loader, bundleName, defaultLocale);
        if (result != null) {
            // check whether we're still responsible for construction -
            // a recursive call to getBundle might have handled it (4300693)
            boolean constructing;
            synchronized (cacheList) {
                cacheKey.setKeyValues(loader, bundleName, defaultLocale);
                constructing = underConstruction.get(cacheKey) == Thread.currentThread();
                cacheKey.clear();
            }
            if (constructing) {
                // set the bundle's parent and put it in the cache
                final ResourceBundle bundle = (ResourceBundle)result;
                if (parent != NOT_FOUND && bundle.parent == null) {
                    bundle.setParent((ResourceBundle) parent);
                }
                bundle.setLocale(baseName, bundleName);
                putBundleInCache(loader, bundleName, defaultLocale, result);
            }
        }
        return result;
    }

    /**
     * Calculate the bundles along the search path from the base bundle to the
     * bundle specified by baseName and locale.
     * @param baseName the base bundle name
     * @param locale the locale
     * @param names the vector used to return the names of the bundles along
     * the search path.
     *
     */
    private static Vector calculateBundleNames(String baseName, Locale locale) {
        final Vector result = new Vector(MAX_BUNDLES_SEARCHED);
        final String language = locale.getLanguage();
        final int languageLength = language.length();
        final String country = locale.getCountry();
        final int countryLength = country.length();
        final String variant = locale.getVariant();
        final int variantLength = variant.length();

        if (languageLength + countryLength + variantLength == 0) {
            //The locale is "", "", "".
            return result;
        }
        final StringBuffer temp = new StringBuffer(baseName);
        temp.append('_');
        temp.append(language);
        if (languageLength > 0) {
            result.addElement(temp.toString());
        }

        if (countryLength + variantLength == 0) {
            return result;
        }
        temp.append('_');
        temp.append(country);
        if (countryLength > 0) {
            result.addElement(temp.toString());
        }

        if (variantLength == 0) {
            return result;
        }
        temp.append('_');
        temp.append(variant);
        result.addElement(temp.toString());

        return result;
    }

    /**
     * Find a bundle in the cache.
     * @param loader the class loader that is responsible for loading the bundle.
     * @param bundleName the complete name of the bundle including locale extension.
     *      ex. sun.text.resources.LocaleElements_fr_BE
     * @param defaultLocale the default locale at the time getBundle was called
     * @return the cached bundle.  null if the bundle is not in the cache.
     */
    private static Object findBundleInCache(ClassLoader loader, String bundleName,
            Locale defaultLocale) {
        //Synchronize access to cacheList, cacheKey, and underConstruction
        synchronized (cacheList) {
            cacheKey.setKeyValues(loader, bundleName, defaultLocale);
            Object result = cacheList.get(cacheKey);
            cacheKey.clear();
            return result;
        }
    }

    /**
     * Put a new bundle in the cache and notify waiting threads that a new
     * bundle has been put in the cache.
     * @param defaultLocale the default locale at the time getBundle was called
     */
    private static void putBundleInCache(ClassLoader loader, String bundleName,
            Locale defaultLocale, Object value) {
        //we use a static shared cacheKey but we use the lock in cacheList since
        //the key is only used to interact with cacheList.
        synchronized (cacheList) {
            cacheKey.setKeyValues(loader, bundleName, defaultLocale);
            cacheList.put(cacheKey.clone(), value);
            underConstruction.remove(cacheKey);
            cacheKey.clear();
            //notify waiters that we're done constructing the bundle
            cacheList.notifyAll();
        }
    }

    /**
     * Load a bundle through either the specified ClassLoader or from a ".properties" file
     * and return the loaded bundle.
     * @param loader the ClassLoader to use to load the bundle.  If null, the system
     *      ClassLoader is used.
     * @param bundleName the name of the resource to load.  The name should be complete
     *      including a qualified class name followed by the locale extension.
     *      ex. sun.text.resources.LocaleElements_fr_BE
     * @param defaultLocale the default locale at the time getBundle was called
     * @return the bundle or null if none could be found.
     */
    private static Object loadBundle(final ClassLoader loader, String bundleName, Locale defaultLocale) {

	// replace any '/'s with '.'s for the bug-for-bug compatible
	// ClassLoader behavior. (5077272, see also 4872868)
	String correctedBundleName = bundleName.replace('/', '.');

        // Search for class file using class loader
        try {
            Class bundleClass;
            if (loader != null) {
                bundleClass = loader.loadClass(correctedBundleName);
            } else {
                bundleClass = Class.forName(correctedBundleName);
            }
            if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                Object myBundle = bundleClass.newInstance();
                // Creating the instance may have triggered a recursive call to getBundle,
                // in which case the bundle created by the recursive call would be in the
                // cache now (4300693). For consistency, we'd then return the bundle from the cache.
                Object otherBundle = findBundleInCache(loader, bundleName, defaultLocale);
                if (otherBundle != null) {
                    return otherBundle;
                } else {
                    return myBundle;
                }
            }
        } catch (Exception e) {
        } catch (LinkageError e) {
        }

        // Next search for a Properties file.
        final String resName = bundleName.replace('.', '/') + ".properties";
        InputStream stream = (InputStream)java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    if (loader != null) {
                        return loader.getResourceAsStream(resName);
                    } else {
                        return ClassLoader.getSystemResourceAsStream(resName);
                    }
                }
            }
        );

        if (stream != null) {
            // make sure it is buffered
            stream = new java.io.BufferedInputStream(stream);
            try {
                return new PropertyResourceBundle(stream);
            } catch (Exception e) {
            } finally {
                try {
                    stream.close();
                } catch (Exception e) {
                    // to avoid propagating an IOException back into the caller
                    // (I'm assuming this is never going to happen, and if it does,
                    // I'm obeying the precedent of swallowing exceptions set by the
                    // existing code above)
                }
            }
        }
        return null;
    }

    /**
     * Gets an object for the given key from this resource bundle.
     * Returns null if this resource bundle does not contain an
     * object for the given key.
     *
     * @param key the key for the desired object
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @return the object for the given key, or null
     */
    protected abstract Object handleGetObject(String key);

    /**
     * Returns an enumeration of the keys.
     *
     */
    public abstract Enumeration getKeys();
}
