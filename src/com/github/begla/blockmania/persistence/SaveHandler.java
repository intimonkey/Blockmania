package com.github.begla.blockmania.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class SaveHandler {
	private String _path;
	private Yaml _yaml;
	private Map<String, Object> _map;
	
	@SuppressWarnings("unchecked")
	public SaveHandler(String path){
		_path = path;
		_yaml = new Yaml();
		try{
			_map = (Map<String, Object>) _yaml.load(new FileInputStream(path));
		}catch(FileNotFoundException e){
			// nop, accessor will cover for us
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String prop_name, T def) {
		T val = null;
		Map<String, Object> map = getMap();
		
		String parts[] = prop_name.split("\\.");
		// first just see if it's there
		val = (T) map.get(prop_name);
		
		if (val == null){
			
			for (int x=0; x<parts.length-1;x++){
				map = (Map<String, Object>) getMap().get(parts[x]);
			}
		}
		
		if (map != null){
			val = (T) map.get(parts[parts.length-1]);
		}
		if (val == null){
			return def;
		} else {
			return val;
		}
	}
	
	public void set(String name, Object value){
		getMap().put(name, value);
	}
	
	private Map getMap(){
		if (_map == null){
			_map = new LinkedHashMap<String, Object>();
		}
		return _map;
	}
	
	public void save() throws IOException{
		FileWriter fw = new FileWriter(new File(_path));
		try{
			_yaml.dump(getMap(), fw);
		} finally{
			fw.close();
		}
		System.out.println(String.format("save():%s\n", _yaml.dump(getMap())));
	}
		
}
