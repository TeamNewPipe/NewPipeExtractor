package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.InvalidInstanceException;

import javax.annotation.Nullable;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * Instance.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <https://www.gnu.org/licenses/>.
 */

public interface Instance {

    @Nullable
    String getName();

    String getUrl();

    boolean isValid();

    /**
     * Fetch instance metadata.
     * <p>
     * Currently, it only fetches the name and save it in the name attribute.
     *
     * @throws InvalidInstanceException
     */
    void fetchInstanceMetaData() throws InvalidInstanceException;

}
