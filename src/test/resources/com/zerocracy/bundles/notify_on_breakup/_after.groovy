/*
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.bundles.notify_on_breakup

import com.jcabi.xml.XML
import com.zerocracy.Item
import com.zerocracy.Project
import com.zerocracy.pmo.Awards
import org.cactoos.text.FormattedText
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

def exec(Project project, XML xml) {
  Item item = project.acq('test.txt').withCloseable {
    item -> assert (item.path().text.contains(
          'User @paulodamaso is not your student anymore, see §47',
      ) && item.path().text.contains(
          'User @g4s8 is not your mentor anymore , he/she broke up with you, see §47'
      )
    )
  }
//  MatcherAssert.assertThat(
//    'Mentor did not received notification',
//    new Awards(binding.variables.farm, 'g4s8').total(),
//    Matchers.is(256)
//  )
//  MatcherAssert.assertThat(
//    'Student did not received notification',
//    new Awards(binding.variables.farm, 'paulodamaso').total(),
//    Matchers.is(256)
//  )
}
