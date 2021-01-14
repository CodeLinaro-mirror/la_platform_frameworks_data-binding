/*
 * Copyright (C) 2020 The Android Open Source Project
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

package androidx.databinding;

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

/**
 * Helper methods for data binding Ktx features.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ViewDataBindingKtx {

    /**
     * Method object extracted out to attach a listener to a bound StateFlow object.
     */
    private val CREATE_STATE_FLOW_LISTENER =
            CreateWeakListener { viewDataBinding, localFieldId, referenceQueue ->
                StateFlowListener(viewDataBinding, localFieldId, referenceQueue)
                        .listener
            }

    @Suppress("unused") // called by generated code
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @JvmStatic
    fun updateStateFlowRegistration(
            viewDataBinding: ViewDataBinding,
            localFieldId: Int,
            observable: Flow<*>?
    ): Boolean {
        viewDataBinding.mInStateFlowRegisterObserver = true
        try {
            return viewDataBinding.updateRegistration(
                    localFieldId, observable, CREATE_STATE_FLOW_LISTENER
            )
        } finally {
            viewDataBinding.mInStateFlowRegisterObserver = false
        }
    }

    internal class StateFlowListener(
            binder: ViewDataBinding?,
            localFieldId: Int,
            referenceQueue: ReferenceQueue<ViewDataBinding>
    ) : ObservableReference<Flow<Any?>> {
        // keep this weak so that we don't end up leaking the lifecycle owner if the
        // binding is GC'ed. (see: b/176886060)
        private var _lifecycleOwnerRef : WeakReference<LifecycleOwner>? = null
        private var observerJob : Job? = null
        private val listener = WeakListener<Flow<Any?>>(
                binder, localFieldId, this, referenceQueue
        )
        override fun getListener(): WeakListener<Flow<Any?>> {
            return listener
        }

        override fun addListener(target: Flow<Any?>?) {
            val owner = _lifecycleOwnerRef?.get() ?: return
            if (target != null) {
                startCollection(owner, target)
            }
        }

        override fun removeListener(target: Flow<Any?>?) {
            observerJob?.cancel()
            observerJob = null
        }

        private fun startCollection(owner: LifecycleOwner, flow: Flow<Any?>) {
            observerJob?.cancel()
            observerJob = owner.lifecycleScope.launchWhenCreated {
                flow.collect {
                    listener.binder?.handleFieldChange(listener.mLocalFieldId, listener.target, 0)
                }
            }
        }

        override fun setLifecycleOwner(lifecycleOwner: LifecycleOwner?) {
            if (_lifecycleOwnerRef?.get() === lifecycleOwner) {
                return
            }
            observerJob?.cancel()
            if (lifecycleOwner == null) {
                _lifecycleOwnerRef = null
                return
            }
            _lifecycleOwnerRef = WeakReference(lifecycleOwner)
            val target = listener.target
            if (target != null) {
                startCollection(lifecycleOwner, target)
            }
        }
    }
}
