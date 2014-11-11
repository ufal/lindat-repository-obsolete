# coding=utf-8
# This work is licensed!
# pylint: disable=W0702,R0201,C0111,W0613,R0914

"""
    We have a cursor to db database.
"""
from collections import defaultdict

def item2metadata( cursor, item_id ):
    cursor.execute( """
        select metadataschemaregistry.short_id,  metadatafieldregistry.element, metadatafieldregistry.qualifier, metadatavalue.text_value
             from metadatavalue
                inner join metadatafieldregistry on metadatavalue.metadata_field_id =  metadatafieldregistry.metadata_field_id
                inner join metadataschemaregistry on metadatafieldregistry.metadata_schema_id =  metadataschemaregistry.metadata_schema_id
             where
                metadatavalue.item_id = %d
    """ % item_id )
    objs = cursor.fetchall()
    vals = defaultdict(list)
    for s, e, q, v in objs:
        k = "%s.%s" % (s, e)
        if q is not None:
            k += "." + q
        vals[k].append(v)
    return vals 

def db2items( cursor ):
    cursor.execute( """
        select item.item_id, eperson.email, collection.name
             from item 
                inner join eperson on item.submitter_id = eperson.eperson_id
                inner join collection on item.owning_collection = collection.collection_id
    """ )
    objs = cursor.fetchall()
    items = dict([(item_id, 
                    { 
                        "submitter": ep,
                        "collection": col,
                    }
                  ) for item_id, ep, col in objs])
    return items



def do( cursor ):
    """
        Do something with the database.
    """
    items = db2items( cursor )

    for k, v in items.iteritems():
        v["metadata"] = item2metadata(cursor, k)

    for i, (k, v) in enumerate(items.iteritems()):
        if i > 10:
            break
        print k, v["submitter"], v["collection"]
        for k, v in v["metadata"].iteritems():
            print "\t", k, v
