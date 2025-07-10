/*
 * Created by FineFindus on 10.07.25.
 *
 * Copyright (C) 2025 FineFindus <FineFindus@proton.me>
 * ContentAvailability.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.stream;

/**
 *  Availability of the stream.
 *
 *  <p>
 *  A stream may be available to all, restricted to a certain user group
 *  or time.
 *  </p>
 */
public enum ContentAvailability {
    /**
     *  The stream is available to all users.
     */
    AVAILABLE,
    /**
     *  The stream is available to users with a membership.
     */
    MEMBERSHIP,
    /**
     *  The stream is behind a paywall.
     */
    PAID,
    /**
     *  The stream is only available in the future.
     */
    UPCOMING,
}
