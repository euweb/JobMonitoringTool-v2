import React from 'react'
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Grid,
} from '@mui/material'
import { Settings as SettingsIcon } from '@mui/icons-material'
import { useAuthStore } from '@/store/authStore'

const UserSettings: React.FC = () => {
  const { user } = useAuthStore()

  return (
    <Box>
      <Typography variant="h4" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <SettingsIcon />
        User Settings
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Profile Information
              </Typography>
              <Box component="form" sx={{ '& .MuiTextField-root': { mb: 2 } }}>
                <TextField
                  fullWidth
                  label="Username"
                  value={user?.username || ''}
                  disabled
                />
                <TextField
                  fullWidth
                  label="Email"
                  value={user?.email || ''}
                />
                <TextField
                  fullWidth
                  label="First Name"
                  value={user?.firstName || ''}
                />
                <TextField
                  fullWidth
                  label="Last Name"
                  value={user?.lastName || ''}
                />
                <Button variant="contained" color="primary">
                  Save Changes
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Change Password
              </Typography>
              <Box component="form" sx={{ '& .MuiTextField-root': { mb: 2 } }}>
                <TextField
                  fullWidth
                  type="password"
                  label="Current Password"
                />
                <TextField
                  fullWidth
                  type="password"
                  label="New Password"
                />
                <TextField
                  fullWidth
                  type="password"
                  label="Confirm New Password"
                />
                <Button variant="contained" color="primary">
                  Change Password
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}

export default UserSettings