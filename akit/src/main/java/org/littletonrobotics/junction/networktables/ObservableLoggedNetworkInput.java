// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package org.littletonrobotics.junction.networktables;

import edu.wpi.first.wpilibj.DriverStation;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public abstract class ObservableLoggedNetworkInput<T>
    extends LoggedNetworkInput {

    public static final String prefix = "NetworkInputs";
    private final T defaultValue;
    private final List<WeakReference<Consumer<T>>> listeners;

    protected ObservableLoggedNetworkInput(T defaultValue) {
        this.defaultValue = defaultValue;
        this.listeners = new ArrayList<>();
    }

    public abstract T get();

    /** Removes the leading slash from a key. */
    protected static String removeSlash(String key) {
        if (key.startsWith("/")) {
            return key.substring(1);
        } else {
            return key;
        }
    }

    public void addListener(Consumer<T> listener) {
        listeners.add(new WeakReference<>(listener));

        // Only do tunables when not in a match, when in a match, give listener the
        // default value
        if (DriverStation.getMatchType() == DriverStation.MatchType.None) {
            listener.accept(get());
        } else {
            listener.accept(defaultValue);
        }
    }

    public void removeListener(Consumer<T> listener) {
        Iterator<WeakReference<Consumer<T>>> iter = listeners.iterator();
        while (iter.hasNext()) {
            WeakReference<Consumer<T>> ref = iter.next();
            Consumer<T> current = ref.get();
            if (current == null || current == listener) {
                iter.remove();
            }
        }
    }

    protected void notifyListeners() {
        Iterator<WeakReference<Consumer<T>>> iter = listeners.iterator();
        while (iter.hasNext()) {
            WeakReference<Consumer<T>> ref = iter.next();
            Consumer<T> listener = ref.get();
            if (listener != null) {
                listener.accept(get());
            } else {
                iter.remove();
            }
        }
    }
}
