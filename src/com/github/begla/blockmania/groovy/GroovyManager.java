/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

package com.github.begla.blockmania.groovy;

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.game.Blockmania;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages everything related to using Groovy from within Java.
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class GroovyManager {
    /**
     * The Binding allows us to keep variable references around where Groovy can play with them
     */
    private Binding _bind;

    /**
     * Directory where we keep "plugin" files (Groovy scripts we'll run - prolly move this setting elsewhere sometime)
     */
    private final String _pluginsPath = "groovy/plugins";

    /**
     * Initialize the GroovyManager and "share" the given World variable via the Binding
     */
    public GroovyManager() {
        _bind = new Binding();
        loadAllPlugins();
    }

    private void loadAllPlugins() {
        File pluginDir = new File("groovy/plugins");

        File[] plugins = pluginDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().contains(".groovy"))
                    return true;
                return false;
            }
        });

        for (File p : plugins) {
            initializePlugin(p.getName());
        }
    }

    /**
     * Method to initialize a plugin - a.k.a. execute a Groovy script in the plugin dir
     *
     * @param pluginName Name of a particular plugin file to execute
     */
    public void initializePlugin(String pluginName) {
        GroovyScriptEngine gse = null;
        try {
            // Create an engine tied to the dir we keep plugins in
            gse = new GroovyScriptEngine(_pluginsPath);
        } catch (IOException ioe) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to initialize plugin (IOException): " + pluginName + ", reason: " + ioe.toString(), ioe);
            ioe.printStackTrace();
        }

        if (gse != null) {
            try {
                updateBinding();
                // Run the specified plugin
                gse.run(pluginName, _bind);
            } catch (ResourceException re) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ResourceException): " + pluginName + ", reason: " + re.toString(), re);
                re.printStackTrace();
            } catch (ScriptException se) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, "Failed to execute plugin (ScriptException): " + pluginName + ", reason: " + se.toString(), se);
                se.printStackTrace();
            }
        }
    }

    private void updateBinding() {
        _bind.setVariable("blockmania", Blockmania.getInstance());
        _bind.setVariable("configuration", ConfigurationManager.getInstance());
    }

    /**
     * Executes the given command with Groovy.
     *
     * @param consoleString Contains what the user entered into the console
     * @return boolean indicating command success or not
     */
    public boolean runGroovyShell(String consoleString) {
        Blockmania.getInstance().getLogger().log(Level.INFO, "Groovy console about to execute command: " + consoleString);
        updateBinding();
        GroovyShell shell = new GroovyShell(_bind);
        try {
            shell.evaluate(consoleString);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
