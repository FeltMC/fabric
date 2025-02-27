/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.resource.loader;

import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class BuiltinModResourcePackSource implements ResourcePackSource {
	private final String modId;

	public BuiltinModResourcePackSource(String modId) {
		this.modId = modId;
	}

	@Override
	public Text decorate(Text packName) {
		return new TranslatableText("pack.nameAndSource", packName, new TranslatableText("pack.source.builtinMod", modId)).formatted(Formatting.GRAY);
	}
}
