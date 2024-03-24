# Sauron
Keep an eye on your player's item
An anti-dupe and tracking plugin allowing you to make items non-fongible.

Sauron lets you define "tracking rules" and items corresponding to these rules will be tracked.
Tracked items have an additional timestamp and unique UUID identifier.
By comparing the timestamp-UUID association of the item with the one in the database, you can detect duplicates and delete them from your server.

Sauron can help you refund your players items after a bug, without having the doubt that maybe they hid the item from your sight.
Any refunded item is blacklisted and will be cleared (and logged) if used again.

