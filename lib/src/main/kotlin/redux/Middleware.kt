package redux

import redux.api.Dispatcher
import redux.api.Reducer
import redux.api.Store
import redux.api.Store.Creator
import redux.api.Store.Enhancer
import redux.api.Store.Subscriber
import redux.api.enhancer.Middleware

/*
 * Copyright (C) 2016 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

fun <S : Any> applyMiddleware(vararg middlewares: Middleware<S>): Enhancer<S> {
    return Enhancer { next ->
        Creator { reducer, initialState ->
            val store = next.create(reducer, initialState)
            val rootDispatcher = middlewares.foldRight(store as Dispatcher) { middleware, next ->
                Dispatcher { action ->
                    middleware.dispatch(store, next, action)
                }
            }
            object : Store<S> {
                override fun dispatch(action: Any) = rootDispatcher.dispatch(action)

                override fun getState() = store.getState()

                override fun replaceReducer(reducer: Reducer<S>) = store.replaceReducer(reducer)

                override fun subscribe(subscriber: Subscriber) = store.subscribe(subscriber)
            }
        }
    }
}
