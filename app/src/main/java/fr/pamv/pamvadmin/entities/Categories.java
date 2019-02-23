package fr.pamv.pamvadmin.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Categories {

    Map<Integer, String> categories;

    public HashMap<Integer, String> getCategories() {
        return new HashMap<>(categories);
    }

    public List<String> getCategoriesName()
    {
        return new ArrayList<>(categories.values());
    }

    public int getIdFromCategorie(String categorie)
    {
        if (categories.containsValue(categorie))
        {
            for (Integer key : categories.keySet())
            {
                if (categories.get(key).equals(categorie))
                    return key;
            }
        }
        return -1;
    }

    public String getCategory(int id)
    {
        return categories.get(id);
    }
}
