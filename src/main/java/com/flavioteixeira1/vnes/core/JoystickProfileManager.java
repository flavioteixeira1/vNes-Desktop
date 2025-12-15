package com.flavioteixeira1.vnes.core;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.json.*;

import net.java.games.input.Component;

public class JoystickProfileManager {
    private static JoystickProfileManager instance;
    private Map<String, JoystickProfile> profiles;
    private String currentProfileName;
    private String profilesDir;
    
    public static class JoystickProfile {
        private String name;
        private Map<Integer, Integer> player1ButtonMapping;
        private Map<Integer, Integer> player2ButtonMapping;
        private Map<Integer, Integer> player3ButtonMapping;
        private Map<Integer, Integer> player4ButtonMapping;
        private Map<String, Integer[]> player1AxisMapping;
        private Map<String, Integer[]> player2AxisMapping;
        private Map<String, Integer[]> player3AxisMapping;
        private Map<String, Integer[]> player4AxisMapping;
        private Date creationDate;
        private Date lastModified;
        private String description;
        
        public JoystickProfile(String name) {
            this.name = name;
            this.player1ButtonMapping = new HashMap<>();
            this.player2ButtonMapping = new HashMap<>();
            this.player3ButtonMapping = new HashMap<>();
            this.player4ButtonMapping = new HashMap<>();
            this.player1AxisMapping = new HashMap<>();
            this.player2AxisMapping = new HashMap<>();
            this.player3AxisMapping = new HashMap<>();
            this.player4AxisMapping = new HashMap<>();
            this.creationDate = new Date();
            this.lastModified = new Date();
            this.description = "Perfil criado em " + new Date();
        }
        
        // Getters e Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Date getCreationDate() { return creationDate; }
        public Date getLastModified() { return lastModified; }
        public void setLastModified(Date date) { this.lastModified = date; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Map<Integer, Integer> getPlayerButtonMapping(int playerId) {
            switch(playerId) {
                case 0: return player1ButtonMapping;
                case 1: return player2ButtonMapping;
                case 2: return player3ButtonMapping;
                case 3: return player4ButtonMapping;
                default: return player1ButtonMapping;
            }
        }
        
        public Map<String, Integer[]> getPlayerAxisMapping(int playerId) {
            switch(playerId) {
                case 0: return player1AxisMapping;
                case 1: return player2AxisMapping;
                case 2: return player3AxisMapping;
                case 3: return player4AxisMapping;
                default: return player1AxisMapping;
            }
        }
        
        public void setPlayerButtonMapping(int playerId, Map<Integer, Integer> mapping) {
            switch(playerId) {
                case 0: player1ButtonMapping = new HashMap<>(mapping); break;
                case 1: player2ButtonMapping = new HashMap<>(mapping); break;
                case 2: player3ButtonMapping = new HashMap<>(mapping); break;
                case 3: player4ButtonMapping = new HashMap<>(mapping); break;
            }
            lastModified = new Date();
        }
        
        public void setPlayerAxisMapping(int playerId, Map<String, Integer[]> mapping) {
            switch(playerId) {
                case 0: player1AxisMapping = new HashMap<>(mapping); break;
                case 1: player2AxisMapping = new HashMap<>(mapping); break;
                case 2: player3AxisMapping = new HashMap<>(mapping); break;
                case 3: player4AxisMapping = new HashMap<>(mapping); break;
            }
            lastModified = new Date();
        }
        
        public void saveToFile(File file) throws IOException {
            JSONObject json = toJSON();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json.toString(2));
            }
        }
        
        public static JoystickProfile loadFromFile(File file) throws IOException, JSONException {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            JSONObject json = new JSONObject(content.toString());
            return fromJSON(json);
        }
        
        private JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("description", description);
            json.put("creationDate", creationDate.getTime());
            json.put("lastModified", lastModified.getTime());
            
            // Player 1
            json.put("player1Buttons", mapToJSON(player1ButtonMapping));
            json.put("player1Axes", axisMapToJSON(player1AxisMapping));
            
            // Player 2
            json.put("player2Buttons", mapToJSON(player2ButtonMapping));
            json.put("player2Axes", axisMapToJSON(player2AxisMapping));
            
            // Player 3
            json.put("player3Buttons", mapToJSON(player3ButtonMapping));
            json.put("player3Axes", axisMapToJSON(player3AxisMapping));
            
            // Player 4
            json.put("player4Buttons", mapToJSON(player4ButtonMapping));
            json.put("player4Axes", axisMapToJSON(player4AxisMapping));
            
            return json;
        }
        
        private static JoystickProfile fromJSON(JSONObject json) {
            String name = json.getString("name");
            JoystickProfile profile = new JoystickProfile(name);
            
            profile.description = json.optString("description", "");
            profile.creationDate = new Date(json.optLong("creationDate", System.currentTimeMillis()));
            profile.lastModified = new Date(json.optLong("lastModified", System.currentTimeMillis()));
            
            // Player 1
            if (json.has("player1Buttons")) {
                profile.player1ButtonMapping = jsonToMap(json.getJSONObject("player1Buttons"));
            }
            if (json.has("player1Axes")) {
                profile.player1AxisMapping = jsonToAxisMap(json.getJSONObject("player1Axes"));
            }
            
            // Player 2
            if (json.has("player2Buttons")) {
                profile.player2ButtonMapping = jsonToMap(json.getJSONObject("player2Buttons"));
            }
            if (json.has("player2Axes")) {
                profile.player2AxisMapping = jsonToAxisMap(json.getJSONObject("player2Axes"));
            }
            
            // Player 3
            if (json.has("player3Buttons")) {
                profile.player3ButtonMapping = jsonToMap(json.getJSONObject("player3Buttons"));
            }
            if (json.has("player3Axes")) {
                profile.player3AxisMapping = jsonToAxisMap(json.getJSONObject("player3Axes"));
            }
            
            // Player 4
            if (json.has("player4Buttons")) {
                profile.player4ButtonMapping = jsonToMap(json.getJSONObject("player4Buttons"));
            }
            if (json.has("player4Axes")) {
                profile.player4AxisMapping = jsonToAxisMap(json.getJSONObject("player4Axes"));
            }
            
            return profile;
        }
        
        private JSONObject mapToJSON(Map<Integer, Integer> map) {
            JSONObject json = new JSONObject();
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                json.put(entry.getKey().toString(), entry.getValue());
            }
            return json;
        }
        
        private JSONObject axisMapToJSON(Map<String, Integer[]> map) {
            JSONObject json = new JSONObject();
            for (Map.Entry<String, Integer[]> entry : map.entrySet()) {
                JSONArray array = new JSONArray();
                array.put(entry.getValue()[0]);
                array.put(entry.getValue()[1]);
                json.put(entry.getKey(), array);
            }
            return json;
        }
        
        private static Map<Integer, Integer> jsonToMap(JSONObject json) {
            Map<Integer, Integer> map = new HashMap<>();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(Integer.parseInt(key), json.getInt(key));
            }
            return map;
        }
        
        private static Map<String, Integer[]> jsonToAxisMap(JSONObject json) {
            Map<String, Integer[]> map = new HashMap<>();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONArray array = json.getJSONArray(key);
                Integer[] values = new Integer[]{array.getInt(0), array.getInt(1)};
                map.put(key, values);
            }
            return map;
        }
    }
    
    private JoystickProfileManager() {
        profiles = new LinkedHashMap<>();
        profilesDir = System.getProperty("user.home") + File.separator + ".vnes" + File.separator + "profiles";
        
       // Criar diretório se não existir
        File dir = new File(profilesDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("Não foi possível criar diretório de perfis: " + profilesDir);
                // Usar diretório temporário como fallback
                profilesDir = System.getProperty("java.io.tmpdir") + File.separator + "vnes_profiles";
                dir = new File(profilesDir);
                dir.mkdirs();
            }
        }
        
        // Carregar perfis existentes
        loadProfilesFromDisk();
        // Criar perfil padrão se não existir nenhum
        if (profiles.isEmpty()) {
            createDefaultProfile();
        }
        // Garantir que currentProfileName não seja null
        if (currentProfileName == null) {
            currentProfileName = "VNES";
        }
    }
    
    public static synchronized JoystickProfileManager getInstance() {
        if (instance == null) {
            instance = new JoystickProfileManager();
        }
        return instance;
    }
    
    private void createDefaultProfile() {
        JoystickProfile vnesProfile = new JoystickProfile("VNES");
        
        // Player 1 - Mapeamento padrão (NES)
        Map<Integer, Integer> p1Buttons = new HashMap<>();
        p1Buttons.put(0, KeyEvent.VK_Z);      // A button
        p1Buttons.put(1, KeyEvent.VK_X);      // B button  
        p1Buttons.put(2, KeyEvent.VK_ENTER);  // Start
        p1Buttons.put(3, KeyEvent.VK_CONTROL); // Select
        vnesProfile.setPlayerButtonMapping(0, p1Buttons);
        
        Map<String, Integer[]> p1Axes = new HashMap<>();
        p1Axes.put("x", new Integer[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT});
        p1Axes.put("y", new Integer[]{KeyEvent.VK_UP, KeyEvent.VK_DOWN});
        vnesProfile.setPlayerAxisMapping(0, p1Axes);
        
        // Player 2 - Mapeamento padrão
        Map<Integer, Integer> p2Buttons = new HashMap<>();
        p2Buttons.put(0, KeyEvent.VK_NUMPAD7); // A button
        p2Buttons.put(1, KeyEvent.VK_NUMPAD9); // B button  
        p2Buttons.put(2, KeyEvent.VK_NUMPAD1); // Start
        p2Buttons.put(3, KeyEvent.VK_NUMPAD3); // Select
        vnesProfile.setPlayerButtonMapping(1, p2Buttons);
        
        Map<String, Integer[]> p2Axes = new HashMap<>();
        p2Axes.put("x", new Integer[]{KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6});
        p2Axes.put("y", new Integer[]{KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD2});
        vnesProfile.setPlayerAxisMapping(1, p2Axes);
        
        profiles.put("VNES", vnesProfile);
        saveProfileToDisk(vnesProfile);
    }
    
    private void loadProfilesFromDisk() {
        File dir = new File(profilesDir);
        if (!dir.exists()) return;
        
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) return;
        
        for (File file : files) {
            try {
                JoystickProfile profile = JoystickProfile.loadFromFile(file);
                profiles.put(profile.getName(), profile);
                System.out.println("Perfil carregado: " + profile.getName());
            } catch (Exception e) {
                System.err.println("Erro ao carregar perfil " + file.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private void saveProfileToDisk(JoystickProfile profile) {
        try {
            File file = new File(profilesDir + File.separator + profile.getName() + ".json");
            profile.saveToFile(file);
            System.out.println("Perfil salvo: " + profile.getName());
        } catch (Exception e) {
            System.err.println("Erro ao salvar perfil " + profile.getName() + ": " + e.getMessage());
        }
    }
    
    // Métodos públicos para gerenciamento de perfis
    
    public List<String> getProfileNames() {
        if (profiles == null) {return new ArrayList<>();}
        return new ArrayList<>(profiles.keySet());
    }
    
    public JoystickProfile getCurrentProfile() {
        return profiles.get(currentProfileName);
    }
  
    public String getCurrentProfileName() {
        if (currentProfileName == null) {
            // Se não houver perfil atual, usar "VNES" ou o primeiro perfil
            if (!profiles.isEmpty()) {
                currentProfileName = profiles.keySet().iterator().next();
            } else {
                currentProfileName = "VNES";
            }
        }
        return currentProfileName;
    }

    
    public boolean setCurrentProfile(String profileName) {
        if (profiles.containsKey(profileName)) {
            currentProfileName = profileName;
            return true;
        }
        return false;
    }
    
    public boolean createNewProfile(String name, String description) {
        if (profiles.containsKey(name)) {
            return false; // Perfil já existe
        }
        
        JoystickProfile profile = new JoystickProfile(name);
        profile.setDescription(description);
        profiles.put(name, profile);
        saveProfileToDisk(profile);
        return true;
    }
    
    public boolean renameProfile(String oldName, String newName) {
        if (!profiles.containsKey(oldName) || profiles.containsKey(newName)) {
            return false;
        }
        
        JoystickProfile profile = profiles.remove(oldName);
        profile.setName(newName);
        profiles.put(newName, profile);
        
        // Atualizar nome do arquivo
        File oldFile = new File(profilesDir + File.separator + oldName + ".json");
        if (oldFile.exists()) {
            oldFile.delete();
        }
        saveProfileToDisk(profile);
        
        if (currentProfileName.equals(oldName)) {
            currentProfileName = newName;
        }
        
        return true;
    }
    
    public boolean deleteProfile(String profileName) {
        if (!profiles.containsKey(profileName) || profileName.equals("VNES")) {
            return false; // Não pode deletar o perfil padrão
        }
        
        profiles.remove(profileName);
        
        // Deletar arquivo
        File file = new File(profilesDir + File.separator + profileName + ".json");
        if (file.exists()) {
            file.delete();
        }
        
        if (currentProfileName.equals(profileName)) {
            currentProfileName = "VNES";
        }
        
        return true;
    }
    
    public boolean exportProfile(String profileName, File exportFile) {
        if (!profiles.containsKey(profileName)) {
            return false;
        }
        
        try {
            JoystickProfile profile = profiles.get(profileName);
            
            // Garantir extensão .json
            String fileName = exportFile.getName();
            if (!fileName.toLowerCase().endsWith(".json")) {
                exportFile = new File(exportFile.getParentFile(), fileName + ".json");
            }
            
            profile.saveToFile(exportFile);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao exportar perfil: " + e.getMessage());
            return false;
        }
    }
    
    public boolean importProfile(File importFile) {
        try {
            JoystickProfile profile = JoystickProfile.loadFromFile(importFile);
            String profileName = profile.getName();
            
            // Se já existir, adicionar sufixo numérico
            int counter = 1;
            String originalName = profileName;
            while (profiles.containsKey(profileName)) {
                profileName = originalName + " (" + counter + ")";
                counter++;
            }
            
            if (!profileName.equals(originalName)) {
                profile.setName(profileName);
            }
            
            profiles.put(profileName, profile);
            saveProfileToDisk(profile);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao importar perfil: " + e.getMessage());
            return false;
        }
    }
    
    public void saveCurrentStateToProfile(JoystickManager[] managers) {
        JoystickProfile profile = getCurrentProfile();
        if (profile == null) return;
        
        for (int i = 0; i < Math.min(managers.length, 4); i++) {
            if (managers[i] != null) {
                // Salvar mapeamento customizado de botões
                profile.getPlayerButtonMapping(i).clear();
                for (int buttonIdx = 0; buttonIdx < 12; buttonIdx++) {
                    int keyCode = managers[i].getMappedKeyForButton(buttonIdx);
                    if (keyCode != -1) {
                        profile.getPlayerButtonMapping(i).put(buttonIdx, keyCode);
                    }
                }
                //Salvar mapeamento dos eixos
                profile.getPlayerAxisMapping(i).clear(); 
                // Obter todos os eixos disponíveis e salvar seus mapeamentos
                List<Component.Identifier> availableAxes = managers[i].getAvailableAxes();
                for (Component.Identifier axisId : availableAxes) {
                    Integer[] mappedKeys = managers[i].getMappedKeysForAxis(axisId);
                    if (mappedKeys != null && (mappedKeys[0] != -1 || mappedKeys[1] != -1)) {
                        // Usar o identificador como string para a chave
                        profile.getPlayerAxisMapping(i).put(axisId.toString(), mappedKeys);
                    }
                }        
            }
        }
        
        profile.setLastModified(new Date());
        saveProfileToDisk(profile);
    }
    
    public boolean loadProfileToManagers(String profileName, JoystickManager[] managers) {
        if (!profiles.containsKey(profileName)) {
            return false;
        }
        JoystickProfile profile = profiles.get(profileName);
        for (int i = 0; i < Math.min(managers.length, 4); i++) {
            if (managers[i] != null) {
                // Carregar mapeamentos do perfil
                // 1. Carregar botões
                Map<Integer, Integer> buttonMapping = profile.getPlayerButtonMapping(i);
                // Limpar mapeamentos customizados atuais
                managers[i].clearAllCustomMappings();
                // Aplicar mapeamentos do perfil para botões
                for (Map.Entry<Integer, Integer> entry : buttonMapping.entrySet()) {
                    managers[i].setCustomButtonMapping(entry.getKey(), entry.getValue());
                }
                // 2. Carregar eixos
            Map<String, Integer[]> axisMapping = profile.getPlayerAxisMapping(i);
            
            // Obter lista de identificadores de eixos disponíveis
            List<Component.Identifier> availableAxes = managers[i].getAvailableAxes();
            Map<String, Component.Identifier> axisIdMap = new HashMap<>();
            
            // Criar mapa de identificadores por nome
            for (Component.Identifier axisId : availableAxes) {
                axisIdMap.put(axisId.toString(), axisId);
            }
            // Aplicar mapeamentos do perfil para eixos
            for (Map.Entry<String, Integer[]> entry : axisMapping.entrySet()) {
                String axisName = entry.getKey();
                Integer[] keys = entry.getValue();
                
                // Verificar se este eixo existe no joystick atual
                Component.Identifier axisId = axisIdMap.get(axisName);
                if (axisId != null) {
                    managers[i].setCustomAxisMapping(axisId, keys[0], keys[1]);
                } else {
                    // Log para debug
                    System.out.println("Eixo " + axisName + " não encontrado no joystick Player " + (i+1));
                }
            }
                // Ativar uso de mapeamento customizado
                managers[i].setUseCustomMapping(true);
            }
        }
        currentProfileName = profileName;
        return true;
    }
    
    public String getProfileInfo(String profileName) {
        if (!profiles.containsKey(profileName)) {
            return "Perfil não encontrado";
        }
        
        JoystickProfile profile = profiles.get(profileName);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        StringBuilder info = new StringBuilder();
        info.append("Nome: ").append(profile.getName()).append("\n");
        info.append("Descrição: ").append(profile.getDescription()).append("\n");
        info.append("Criado em: ").append(sdf.format(profile.getCreationDate())).append("\n");
        info.append("Modificado em: ").append(sdf.format(profile.getLastModified())).append("\n");
        info.append("\nBotões configurados:\n");
        
        for (int player = 0; player < 4; player++) {
            Map<Integer, Integer> buttons = profile.getPlayerButtonMapping(player);
            if (!buttons.isEmpty()) {
                info.append("  Player ").append(player + 1).append(": ");
                info.append(buttons.size()).append(" botões\n");
            }
        }
        
        return info.toString();
    }
}