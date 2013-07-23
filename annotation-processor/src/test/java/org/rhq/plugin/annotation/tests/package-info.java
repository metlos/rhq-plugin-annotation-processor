
@Name("TestPlugin")
@Description("Test plugin for the annotation processor")
@AgentPlugin(version = "1.0.0-SNAPSHOT", dependencies = @AgentPlugin.Dependency(pluginName = "fake", useClasses = true))
package org.rhq.plugin.annotation.tests;

import org.rhq.plugin.annotation.AgentPlugin;
import org.rhq.plugin.annotation.common.Description;
import org.rhq.plugin.annotation.common.Name;
