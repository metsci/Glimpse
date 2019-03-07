/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.var2;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;
import static com.metsci.glimpse.var2.ListenablePair.ALL;
import static com.metsci.glimpse.var2.ListenablePair.COMPLETED;
import static com.metsci.glimpse.var2.ListenerFlag.EMPTY_FLAGS;
import static com.metsci.glimpse.var2.ListenerFlag.IMMEDIATE;
import static com.metsci.glimpse.var2.ListenerFlag.ONCE;
import static com.metsci.glimpse.var2.ListenerFlag.flags;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.VarEvent;

public class VarUtils
{

    @SafeVarargs
    public static Listenable listenable( Listenable... listenables )
    {
        return listenable( asList( listenables ) );
    }

    public static Listenable listenable( Collection<? extends Listenable> listenables )
    {
        return new ListenableSet( listenables );
    }

    @SafeVarargs
    public static Listenable completedListenable( ListenablePair... pairs )
    {
        return completedListenable( asList( pairs ) );
    }

    public static Listenable completedListenable( Collection<? extends ListenablePair> pairs )
    {
        return listenable( mapCollection( pairs, COMPLETED ) );
    }

    @SafeVarargs
    public static Listenable allListenable( ListenablePair... pairs )
    {
        return completedListenable( asList( pairs ) );
    }

    public static Listenable allListenable( Collection<? extends ListenablePair> pairs )
    {
        return listenable( mapCollection( pairs, ALL ) );
    }

    @SafeVarargs
    public static ListenablePair listenablePair( ListenablePair... pairs )
    {
        return listenablePair( asList( pairs ) );
    }

    public static ListenablePair listenablePair( Collection<? extends ListenablePair> pairs )
    {
        return new ListenablePairSet( pairs );
    }

    public static <T,R> Collection<R> mapCollection( Collection<T> ts, Function<? super T,? extends R> fn )
    {
        Collection<R> rs = new ArrayList<>( ts.size( ) );
        for ( T t : ts )
        {
            rs.add( fn.apply( t ) );
        }
        return rs;
    }

    public static <K,V,K2 extends K> Var<V> mapValueVar( Var<ImmutableMap<K,V>> mapVar, K2 key )
    {
        return new VarDerived<V>( mapVar )
        {
            @Override
            public V v( )
            {
                return mapVar.v( ).get( key );
            }

            @Override
            public boolean set( boolean ongoing, V value )
            {
                return putMapValue( mapVar, ongoing, key, value );
            }
        };
    }

    public static <K,V,K2 extends K> Var<V> mapValueVar( Var<ImmutableMap<K,V>> mapVar, ReadableVar<K2> keyVar )
    {
        return new VarDerived<V>( mapVar, keyVar )
        {
            @Override
            public V v( )
            {
                return mapVar.v( ).get( keyVar.v( ) );
            }

            @Override
            public boolean set( boolean ongoing, V value )
            {
                return putMapValue( mapVar, ongoing, keyVar.v( ), value );
            }
        };
    }

    /**
     * When modifying the returned var, don't add mappings for keys that are not in {@code keysVar}.
     * Such mappings will be ignored.
     * <p>
     * Currently, no attempt is made to preserve the iteration order of {@code mapVar}. This might
     * change in the future.
     */
    public static <K,V,K2 extends K> Var<ImmutableMap<K,V>> mapSubsetVar( Var<ImmutableMap<K,V>> mapVar, ReadableVar<? extends Set<K2>> keysVar )
    {
        return new VarDerived<ImmutableMap<K,V>>( mapVar, keysVar )
        {
            protected Map<K,V> mapCached = null;
            protected Set<K2> keysCached = null;
            protected ImmutableMap<K,V> submapCached = null;

            @Override
            public ImmutableMap<K,V> v( )
            {
                Map<K,V> map = mapVar.v( );
                Set<K2> keys = keysVar.v( );
                if ( map == this.mapCached && keys == this.keysCached )
                {
                    return this.submapCached;
                }
                else
                {
                    Map<K,V> submap = new LinkedHashMap<>( );
                    for ( K2 key : keys )
                    {
                        V value = map.get( key );
                        if ( value != null )
                        {
                            submap.put( key, value );
                        }
                    }
                    this.mapCached = map;
                    this.keysCached = keys;
                    this.submapCached = ImmutableMap.copyOf( submap );
                    return this.submapCached;
                }
            }

            @Override
            public boolean set( boolean ongoing, ImmutableMap<K,V> submap )
            {
                return mapVar.updateIfNonNull( ongoing, oldMap ->
                {
                    Map<K,V> newMap = new LinkedHashMap<>( oldMap );
                    for ( K2 key : keysVar.v( ) )
                    {
                        V value = submap.get( key );
                        if ( value == null )
                        {
                            newMap.remove( key );
                        }
                        else
                        {
                            newMap.put( key, value );
                        }
                    }
                    return ImmutableMap.copyOf( newMap );
                } );
            }
        };
    }

    public static <A,B> Var<B> propertyVar( Var<A> ownerVar, Function<? super A,? extends B> getFn, BiFunction<? super A,B,? extends A> updateFn )
    {
        return new VarDerived<B>( ownerVar )
        {
            @Override
            public B v( )
            {
                A owner = ownerVar.v( );
                return ( owner == null ? null : getFn.apply( owner ) );
            }

            @Override
            public boolean set( boolean ongoing, B value )
            {
                return ownerVar.updateIfNonNull( ongoing, owner -> updateFn.apply( owner, value ) );
            }
        };
    }

    public static <A,B> ReadableVar<B> propertyVar( ReadableVar<A> ownerVar, Function<? super A,? extends B> getFn )
    {
        return new ReadableVarDerived<B>( ownerVar )
        {
            @Override
            public B v( )
            {
                A owner = ownerVar.v( );
                return ( owner == null ? null : getFn.apply( owner ) );
            }
        };
    }

    public static <K,V,K2 extends K,V2 extends V> boolean putMapValue( Var<ImmutableMap<K,V>> mapVars, K2 key, V2 value )
    {
        return putMapValue( mapVars, false, key, value );
    }

    public static <K,V,K2 extends K,V2 extends V> boolean putMapValue( Var<ImmutableMap<K,V>> mapVars, boolean ongoing, K2 key, V2 value )
    {
        return mapVars.update( ongoing, map -> mapWith( map, key, value ) );
    }

    public static <K,V,K2 extends K> boolean updateMapValue( Var<ImmutableMap<K,V>> mapVars, K2 key, Function<? super V,? extends V> updateFn )
    {
        return updateMapValue( mapVars, false, key, updateFn );
    }

    public static <K,V,K2 extends K> boolean updateMapValue( Var<ImmutableMap<K,V>> mapVars, boolean ongoing, K2 key, Function<? super V,? extends V> updateFn )
    {
        return mapVars.update( ongoing, map -> mapWith( map, key, updateFn ) );
    }

    public static ListenablePair wrapListenable1( com.metsci.glimpse.util.var.Listenable<VarEvent> listenable1 )
    {
        return new ListenablePair( )
        {
            protected final Listenable ongoing = wrapListenable1( listenable1, ev -> ev.ongoing );
            protected final Listenable completed = wrapListenable1( listenable1, ev -> !ev.ongoing );
            protected final Listenable all = wrapListenable1( listenable1, ev -> true );

            @Deprecated
            @Override
            public Listenable ongoing( )
            {
                return this.ongoing;
            }

            @Override
            public Listenable completed( )
            {
                return this.completed;
            }

            @Override
            public Listenable all( )
            {
                return this.all;
            }

            @Override
            public Disposable addListener( Set<? extends ListenerFlag> flags, ListenablePairListener listener )
            {
                return doHandleImmediateFlag( flags, listener, flags2 ->
                {
                    return doAddPairListener( this.ongoing, this.completed, flags2, listener );
                } );
            }
        };
    }

    public static Listenable wrapListenable1( com.metsci.glimpse.util.var.Listenable<VarEvent> listenable1, Predicate<VarEvent> filter )
    {
        return new Listenable( )
        {
            @Override
            public Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
            {
                if ( flags.contains( IMMEDIATE ) )
                {
                    listener.run( );
                    if ( flags.contains( ONCE ) )
                    {
                        return ( ) -> { };
                    }
                }

                DisposableGroup disposables = new DisposableGroup( );
                if ( flags.contains( ONCE ) )
                {
                    disposables.add( listenable1.addListener( false, ev ->
                    {
                        if ( filter.test( ev ) )
                        {
                            listener.run( );
                            disposables.dispose( );
                            disposables.clear( );
                        }
                    } ) );
                }
                else
                {
                    disposables.add( listenable1.addListener( false, ev ->
                    {
                        if ( filter.test( ev ) )
                        {
                            listener.run( );
                        }
                    } ) );
                }
                return disposables;
            }
        };
    }

    public static <V> Var<V> wrapVar1( com.metsci.glimpse.util.var.Var<V> var1 )
    {
        return new VarDerived<V>( wrapListenable1( var1 ) )
        {
            @Override
            public V v( )
            {
                return var1.v( );
            }

            @Override
            public boolean set( boolean ongoing, V value )
            {
                V oldValue = var1.v( );
                var1.set( ongoing, value );
                return ( var1.v( ) != oldValue );
            }
        };
    }

    public static interface OldNewListener<V>
    {
        void accept( V vOld, V vNew );
    }

    public static interface OldNewPairListener<V>
    {
        void accept( boolean ongoing, V vOld, V vNew );
    }

    public static <V0,V extends V0> Disposable addOldNewListener( ReadableVar<V> var,
                                                                  Function<? super ReadableVar<V>,Listenable> member,
                                                                  OldNewListener<V0> listener )
    {
        return addOldNewListener( var, member, EMPTY_FLAGS, listener );
    }

    public static <V0,V extends V0> Disposable addOldNewListener( ReadableVar<V> var,
                                                                  Function<? super ReadableVar<V>,Listenable> member,
                                                                  ListenerFlag flag,
                                                                  OldNewListener<V0> listener )
    {
        return addOldNewListener( var, member, flags( flag ), listener );
    }

    public static <V0,V extends V0> Disposable addOldNewListener( ReadableVar<V> var,
                                                                  Function<? super ReadableVar<V>,Listenable> member,
                                                                  Set<? extends ListenerFlag> flags,
                                                                  OldNewListener<V0> listener )
    {
        if ( flags.contains( IMMEDIATE ) )
        {
            listener.accept( null, var.v( ) );
            if ( flags.contains( ONCE ) )
            {
                return ( ) -> { };
            }
        }

        Set<ListenerFlag> flags2 = setMinus( ImmutableSet.copyOf( flags ), IMMEDIATE );
        return member.apply( var ).addListener( flags2, new Runnable( )
        {
            V value = var.v( );

            @Override
            public void run( )
            {
                V oldValue = this.value;
                V newValue = var.v( );
                if ( !equal( newValue, oldValue ) )
                {
                    this.value = newValue;
                    listener.accept( oldValue, newValue );
                }
            }
        } );
    }

    public static <V0,V extends V0> Disposable addOldNewListener( ReadableVar<V> var,
                                                                  OldNewPairListener<V0> listener )
    {
        return addOldNewListener( var, EMPTY_FLAGS, listener );
    }

    public static <V0,V extends V0> Disposable addOldNewListener( ReadableVar<V> var,
                                                                  ListenerFlag flag,
                                                                  OldNewPairListener<V0> listener )
    {
        return addOldNewListener( var, flags( flag ), listener );
    }

    public static <V0,V extends V0> Disposable addOldNewListener( ReadableVar<V> var,
                                                                  Set<? extends ListenerFlag> flags,
                                                                  OldNewPairListener<V0> listener )
    {
        if ( flags.contains( IMMEDIATE ) )
        {
            listener.accept( false, null, var.v( ) );
            if ( flags.contains( ONCE ) )
            {
                return ( ) -> { };
            }
        }

        Set<ListenerFlag> flags2 = setMinus( ImmutableSet.copyOf( flags ), IMMEDIATE );
        return var.addListener( flags2, new ListenablePairListener( )
        {
            V value = var.v( );
            boolean hasOngoingChanges = false;

            @Override
            public void run( boolean ongoing )
            {
                V oldValue = this.value;
                V newValue = var.v( );
                if ( ( !ongoing && this.hasOngoingChanges ) || !equal( newValue, oldValue ) )
                {
                    // Update current value
                    this.value = newValue;

                    // Keep track of whether we've seen any ongoing changes since
                    // the last completed change -- the current change is either
                    // ongoing (set the flag), or completed (clear the flag)
                    this.hasOngoingChanges = ongoing;

                    // Fire listeners
                    listener.accept( ongoing, oldValue, newValue );
                }
            }
        } );
    }

    public static <V> Listenable filterListenable( Listenable rawListenable, Supplier<V> valueFn )
    {
        return new Listenable( )
        {
            @Override
            public Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
            {
                if ( flags.contains( IMMEDIATE ) )
                {
                    listener.run( );
                    if ( flags.contains( ONCE ) )
                    {
                        return ( ) -> { };
                    }
                }
                Set<ListenerFlag> flags2 = setMinus( ImmutableSet.copyOf( flags ), IMMEDIATE );
                return rawListenable.addListener( flags2, filterListener( listener, valueFn ) );
            }
        };
    }

    public static <V> Runnable filterListener( Runnable rawListener, Supplier<V> valueFn )
    {
        return new Runnable( )
        {
            V value = valueFn.get( );

            @Override
            public void run( )
            {
                V oldValue = this.value;
                V newValue = valueFn.get( );
                if ( !equal( newValue, oldValue ) )
                {
                    this.value = newValue;
                    rawListener.run( );
                }
            }
        };
    }

    public static <V> ListenablePairListener filterListener( ListenablePairListener rawListener, Supplier<V> valueFn )
    {
        return new ListenablePairListener( )
        {
            V value = valueFn.get( );
            boolean hasOngoingChanges = false;

            @Override
            public void run( boolean ongoing )
            {
                V oldValue = this.value;
                V newValue = valueFn.get( );
                if ( ( !ongoing && this.hasOngoingChanges ) || !equal( newValue, oldValue ) )
                {
                    // Update current value
                    this.value = newValue;

                    // Keep track of whether we've seen any ongoing changes since
                    // the last completed change -- the current change is either
                    // ongoing (set the flag), or completed (clear the flag)
                    this.hasOngoingChanges = ongoing;

                    // Fire listeners
                    rawListener.run( ongoing );
                }
            }
        };
    }

    public static Disposable doHandleImmediateFlag( Set<? extends ListenerFlag> flags,
                                                    Runnable immediateListener,
                                                    Function<? super Set<? extends ListenerFlag>,? extends Disposable> doAddListener )
    {
        if ( flags.contains( IMMEDIATE ) )
        {
            immediateListener.run( );
            if ( flags.contains( ONCE ) )
            {
                return ( ) -> { };
            }
        }
        Set<ListenerFlag> flags2 = setMinus( ImmutableSet.copyOf( flags ), IMMEDIATE );
        return doAddListener.apply( flags2 );
    }

    public static Disposable doHandleImmediateFlag( Set<? extends ListenerFlag> flags,
                                                    ListenablePairListener listener,
                                                    Function<? super Set<? extends ListenerFlag>,? extends Disposable> doAddListener )
    {
        Runnable immediateListener = ( ) -> listener.run( false );
        return doHandleImmediateFlag( flags, immediateListener, doAddListener );
    }

    public static Disposable doAddPairListener( Listenable ongoing,
                                                Listenable completed,
                                                Set<? extends ListenerFlag> flags,
                                                ListenablePairListener listener )
    {
        DisposableGroup disposables = new DisposableGroup( );
        if ( flags.contains( ONCE ) )
        {
            Set<ListenerFlag> flags2 = setMinus( ImmutableSet.copyOf( flags ), ONCE );

            disposables.add( ongoing.addListener( flags2, ( ) ->
            {
                listener.run( true );
                disposables.dispose( );
                disposables.clear( );
            } ) );

            disposables.add( completed.addListener( flags2, ( ) ->
            {
                listener.run( false );
                disposables.dispose( );
                disposables.clear( );
            } ) );
        }
        else
        {
            disposables.add( ongoing.addListener( flags, ( ) ->
            {
                listener.run( true );
            } ) );

            disposables.add( completed.addListener( flags, ( ) ->
            {
                listener.run( false );
            } ) );
        }
        return disposables;
    }

    public static interface OldNewMapEntryListener<K,V>
    {
        void accept( K key, V vOld, V vNew );
    }

    public static interface OldNewMapEntryPairListener<K,V>
    {
        void accept( boolean ongoing, K key, V vOld, V vNew );
    }

    public static <K0,V0,K extends K0,V extends V0> Disposable onMapEntryChanged( ReadableVar<? extends ImmutableMap<K,V>> var,
                                                                                  Function<? super ReadableVar<? extends ImmutableMap<K,V>>,Listenable> member,
                                                                                  OldNewMapEntryListener<K0,V0> listener )
    {
        return onMapEntryChanged( var, member, EMPTY_FLAGS, listener );
    }

    public static <K0,V0,K extends K0,V extends V0> Disposable onMapEntryChanged( ReadableVar<? extends ImmutableMap<K,V>> var,
                                                                                  Function<? super ReadableVar<? extends ImmutableMap<K,V>>,Listenable> member,
                                                                                  ListenerFlag flag,
                                                                                  OldNewMapEntryListener<K0,V0> listener )
    {
        return onMapEntryChanged( var, member, flags( flag ), listener );
    }

    public static <K0,V0,K extends K0,V extends V0> Disposable onMapEntryChanged( ReadableVar<? extends ImmutableMap<K,V>> var,
                                                                                  Function<? super ReadableVar<? extends ImmutableMap<K,V>>,Listenable> member,
                                                                                  Set<? extends ListenerFlag> flags,
                                                                                  OldNewMapEntryListener<K0,V0> listener )
    {
        return addOldNewListener( var, member, flags, ( mapOld0, mapNew0 ) ->
        {
            Map<K,V> mapOld = firstNonNull( mapOld0, ImmutableMap.of( ) );
            Map<K,V> mapNew = firstNonNull( mapNew0, ImmutableMap.of( ) );
            for ( K k : union( mapOld.keySet( ), mapNew.keySet( ) ) )
            {
                V vOld = mapOld.get( k );
                V vNew = mapNew.get( k );
                if ( !equal( vNew, vOld ) )
                {
                    listener.accept( k, vOld, vNew );
                }
            }
        } );
    }

    public static <K0,V0,K extends K0,V extends V0> Disposable onMapEntryChanged( ReadableVar<? extends ImmutableMap<K,V>> var,
                                                                                  OldNewMapEntryPairListener<K0,V0> listener )
    {
        return onMapEntryChanged( var, EMPTY_FLAGS, listener );
    }

    public static <K0,V0,K extends K0,V extends V0> Disposable onMapEntryChanged( ReadableVar<? extends ImmutableMap<K,V>> var,
                                                                                  ListenerFlag flag,
                                                                                  OldNewMapEntryPairListener<K0,V0> listener )
    {
        return onMapEntryChanged( var, flags( flag ), listener );
    }

    public static <K0,V0,K extends K0,V extends V0> Disposable onMapEntryChanged( ReadableVar<? extends ImmutableMap<K,V>> var,
                                                                                  Set<? extends ListenerFlag> flags,
                                                                                  OldNewMapEntryPairListener<K0,V0> listener )
    {
        return addOldNewListener( var, flags, ( ongoing, mapOld0, mapNew0 ) ->
        {
            Map<K,V> mapOld = firstNonNull( mapOld0, ImmutableMap.of( ) );
            Map<K,V> mapNew = firstNonNull( mapNew0, ImmutableMap.of( ) );
            for ( K k : union( mapOld.keySet( ), mapNew.keySet( ) ) )
            {
                V vOld = mapOld.get( k );
                V vNew = mapNew.get( k );
                if ( !equal( vNew, vOld ) )
                {
                    listener.accept( ongoing, k, vOld, vNew );
                }
            }
        } );
    }

    public static <K> Disposable onMapKeyAdded( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                Function<? super ReadableVar<? extends ImmutableMap<K,?>>,Listenable> member,
                                                Consumer<? super K> listener )
    {
        return onMapKeyAdded( var, member, EMPTY_FLAGS, listener );
    }

    public static <K> Disposable onMapKeyAdded( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                Function<? super ReadableVar<? extends ImmutableMap<K,?>>,Listenable> member,
                                                ListenerFlag flag,
                                                Consumer<? super K> listener )
    {
        return onMapKeyAdded( var, member, flags( flag ), listener );
    }

    public static <K> Disposable onMapKeyAdded( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                Function<? super ReadableVar<? extends ImmutableMap<K,?>>,Listenable> member,
                                                Set<? extends ListenerFlag> flags,
                                                Consumer<? super K> listener )
    {
        return addOldNewListener( var, member, flags, ( mapOld0, mapNew0 ) ->
        {
            Map<K,?> mapOld = firstNonNull( mapOld0, ImmutableMap.of( ) );
            Map<K,?> mapNew = firstNonNull( mapNew0, ImmutableMap.of( ) );
            for ( K k : difference( mapNew.keySet( ), mapOld.keySet( ) ) )
            {
                listener.accept( k );
            }
        } );
    }

    public interface PairConsumer<T>
    {
        void accept( boolean ongoing, T t );
    }

    public static <K> Disposable onMapKeyAdded( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                PairConsumer<? super K> listener )
    {
        return onMapKeyAdded( var, EMPTY_FLAGS, listener );
    }

    public static <K> Disposable onMapKeyAdded( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                ListenerFlag flag,
                                                PairConsumer<? super K> listener )
    {
        return onMapKeyAdded( var, flags( flag ), listener );
    }

    public static <K> Disposable onMapKeyAdded( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                Set<? extends ListenerFlag> flags,
                                                PairConsumer<? super K> listener )
    {
        return addOldNewListener( var, flags, ( ongoing, mapOld0, mapNew0 ) ->
        {
            Map<K,?> mapOld = firstNonNull( mapOld0, ImmutableMap.of( ) );
            Map<K,?> mapNew = firstNonNull( mapNew0, ImmutableMap.of( ) );
            for ( K k : difference( mapNew.keySet( ), mapOld.keySet( ) ) )
            {
                listener.accept( ongoing, k );
            }
        } );
    }

    public static <K> Disposable onMapKeyRemoved( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                  Function<? super ReadableVar<? extends ImmutableMap<K,?>>,Listenable> member,
                                                  Consumer<? super K> listener )
    {
        return onMapKeyRemoved( var, member, EMPTY_FLAGS, listener );
    }

    public static <K> Disposable onMapKeyRemoved( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                  Function<? super ReadableVar<? extends ImmutableMap<K,?>>,Listenable> member,
                                                  ListenerFlag flag,
                                                  Consumer<? super K> listener )
    {
        return onMapKeyRemoved( var, member, flags( flag ), listener );
    }

    public static <K> Disposable onMapKeyRemoved( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                  Function<? super ReadableVar<? extends ImmutableMap<K,?>>,Listenable> member,
                                                  Set<? extends ListenerFlag> flags,
                                                  Consumer<? super K> listener )
    {
        return addOldNewListener( var, member, flags, ( mapOld0, mapNew0 ) ->
        {
            Map<K,?> mapOld = firstNonNull( mapOld0, ImmutableMap.of( ) );
            Map<K,?> mapNew = firstNonNull( mapNew0, ImmutableMap.of( ) );
            for ( K k : difference( mapOld.keySet( ), mapNew.keySet( ) ) )
            {
                listener.accept( k );
            }
        } );
    }

    public static <K> Disposable onMapKeyRemoved( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                  PairConsumer<? super K> listener )
    {
        return onMapKeyRemoved( var, EMPTY_FLAGS, listener );
    }

    public static <K> Disposable onMapKeyRemoved( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                  ListenerFlag flag,
                                                  PairConsumer<? super K> listener )
    {
        return onMapKeyRemoved( var, flags( flag ), listener );
    }

    public static <K> Disposable onMapKeyRemoved( ReadableVar<? extends ImmutableMap<K,?>> var,
                                                  Set<? extends ListenerFlag> flags,
                                                  PairConsumer<? super K> listener )
    {
        return addOldNewListener( var, flags, ( ongoing, mapOld0, mapNew0 ) ->
        {
            Map<K,?> mapOld = firstNonNull( mapOld0, ImmutableMap.of( ) );
            Map<K,?> mapNew = firstNonNull( mapNew0, ImmutableMap.of( ) );
            for ( K k : difference( mapOld.keySet( ), mapNew.keySet( ) ) )
            {
                listener.accept( ongoing, k );
            }
        } );
    }

    public static <T> Disposable onElementAdded( ReadableVar<? extends ImmutableCollection<T>> var,
                                                 Function<? super ReadableVar<? extends ImmutableCollection<T>>,Listenable> member,
                                                 Consumer<? super T> listener )
    {
        return onElementAdded( var, member, EMPTY_FLAGS, listener );
    }

    public static <T> Disposable onElementAdded( ReadableVar<? extends ImmutableCollection<T>> var,
                                                 Function<? super ReadableVar<? extends ImmutableCollection<T>>,Listenable> member,
                                                 ListenerFlag flag,
                                                 Consumer<? super T> listener )
    {
        return onElementAdded( var, member, flags( flag ), listener );
    }

    public static <T> Disposable onElementAdded( ReadableVar<? extends ImmutableCollection<T>> var,
                                                 Function<? super ReadableVar<? extends ImmutableCollection<T>>,Listenable> member,
                                                 Set<? extends ListenerFlag> flags,
                                                 Consumer<? super T> listener )
    {
        return addOldNewListener( var, member, flags, ( setOld0, setNew0 ) ->
        {
            Set<T> setOld = ImmutableSet.copyOf( firstNonNull( setOld0, ImmutableSet.of( ) ) );
            Set<T> setNew = ImmutableSet.copyOf( firstNonNull( setNew0, ImmutableSet.of( ) ) );
            for ( T t : difference( setNew, setOld ) )
            {
                listener.accept( t );
            }
        } );
    }

    public static <T> Disposable onElementAdded( ReadableVar<? extends ImmutableCollection<T>> var,
                                                 PairConsumer<? super T> listener )
    {
        return onElementAdded( var, EMPTY_FLAGS, listener );
    }

    public static <T> Disposable onElementAdded( ReadableVar<? extends ImmutableCollection<T>> var,
                                                 ListenerFlag flag,
                                                 PairConsumer<? super T> listener )
    {
        return onElementAdded( var, flags( flag ), listener );
    }

    public static <T> Disposable onElementAdded( ReadableVar<? extends ImmutableCollection<T>> var,
                                                 Set<? extends ListenerFlag> flags,
                                                 PairConsumer<? super T> listener )
    {
        return addOldNewListener( var, flags, ( ongoing, setOld0, setNew0 ) ->
        {
            Set<T> setOld = ImmutableSet.copyOf( firstNonNull( setOld0, ImmutableSet.of( ) ) );
            Set<T> setNew = ImmutableSet.copyOf( firstNonNull( setNew0, ImmutableSet.of( ) ) );
            for ( T t : difference( setNew, setOld ) )
            {
                listener.accept( ongoing, t );
            }
        } );
    }

    public static <T> Disposable onElementRemoved( ReadableVar<? extends ImmutableCollection<T>> var,
                                                   Function<? super ReadableVar<? extends ImmutableCollection<T>>,Listenable> member,
                                                   Consumer<? super T> listener )
    {
        return onElementRemoved( var, member, EMPTY_FLAGS, listener );
    }

    public static <T> Disposable onElementRemoved( ReadableVar<? extends ImmutableCollection<T>> var,
                                                   Function<? super ReadableVar<? extends ImmutableCollection<T>>,Listenable> member,
                                                   ListenerFlag flag,
                                                   Consumer<? super T> listener )
    {
        return onElementRemoved( var, member, flags( flag ), listener );
    }

    public static <T> Disposable onElementRemoved( ReadableVar<? extends ImmutableCollection<T>> var,
                                                   Function<? super ReadableVar<? extends ImmutableCollection<T>>,Listenable> member,
                                                   Set<? extends ListenerFlag> flags,
                                                   Consumer<? super T> listener )
    {
        return addOldNewListener( var, member, flags, ( setOld0, setNew0 ) ->
        {
            Set<T> setOld = ImmutableSet.copyOf( firstNonNull( setOld0, ImmutableSet.of( ) ) );
            Set<T> setNew = ImmutableSet.copyOf( firstNonNull( setNew0, ImmutableSet.of( ) ) );
            for ( T t : difference( setOld, setNew ) )
            {
                listener.accept( t );
            }
        } );
    }

    public static <T> Disposable onElementRemoved( ReadableVar<? extends ImmutableCollection<T>> var,
                                                   PairConsumer<? super T> listener )
    {
        return onElementRemoved( var, EMPTY_FLAGS, listener );
    }

    public static <T> Disposable onElementRemoved( ReadableVar<? extends ImmutableCollection<T>> var,
                                                   ListenerFlag flag,
                                                   PairConsumer<? super T> listener )
    {
        return onElementRemoved( var, flags( flag ), listener );
    }

    public static <T> Disposable onElementRemoved( ReadableVar<? extends ImmutableCollection<T>> var,
                                                   Set<? extends ListenerFlag> flags,
                                                   PairConsumer<? super T> listener )
    {
        return addOldNewListener( var, flags, ( ongoing, setOld0, setNew0 ) ->
        {
            Set<T> setOld = ImmutableSet.copyOf( firstNonNull( setOld0, ImmutableSet.of( ) ) );
            Set<T> setNew = ImmutableSet.copyOf( firstNonNull( setNew0, ImmutableSet.of( ) ) );
            for ( T t : difference( setOld, setNew ) )
            {
                listener.accept( ongoing, t );
            }
        } );
    }

    protected static <K,V> ImmutableMap<K,V> mapWith( ImmutableMap<K,V> map, K key, Function<? super V,? extends V> transformFn )
    {
        V value = transformFn.apply( map.get( key ) );
        return mapWith( map, key, value );
    }

    protected static <K,V> ImmutableMap<K,V> mapWith( ImmutableMap<K,V> map, K key, V value )
    {
        if ( equal( value, map.get( key ) ) )
        {
            return map;
        }
        else
        {
            Map<K,V> newMap = new LinkedHashMap<>( map );
            if ( value == null )
            {
                newMap.remove( key );
            }
            else
            {
                newMap.put( key, value );
            }
            return ImmutableMap.copyOf( newMap );
        }
    }

    @SafeVarargs
    protected static <V> ImmutableSet<V> setMinus( ImmutableSet<V> set, V... values )
    {
        Set<V> newSet = new LinkedHashSet<>( set );

        boolean changed = false;
        for ( V value : values )
        {
            changed |= newSet.remove( value );
        }

        return ( changed ? ImmutableSet.copyOf( newSet ) : set );
    }

}
